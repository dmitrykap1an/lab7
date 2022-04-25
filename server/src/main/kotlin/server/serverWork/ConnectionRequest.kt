package server.serverWork

import client.Managers.UserSerialize
import general.AppIO.Answer
import org.postgresql.util.PSQLException
import server.Database.PasswordHasher
import server.Managers.CommandManager
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.channels.*
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

class ConnectionRequest : Runnable {
    private var registrationStatus = false
    private var clientSocket : SocketChannel
    private val commandManager : CommandManager
    private lateinit var nameOfUser : String;
    private val executor = Executors.newFixedThreadPool(10)
    private val semaphore = Semaphore(1, false)
    private lateinit var outt: ObjectOutputStream
    private lateinit var inn: ObjectInputStream

    constructor(clientSocket : SocketChannel, commandManager: CommandManager){
        this.clientSocket = clientSocket
        this.commandManager = commandManager
    }

    override fun run(){

        outt = ObjectOutputStream(clientSocket.socket().outputStream)
        inn = ObjectInputStream(clientSocket.socket().inputStream)
        userChecker()
        executor.submit(MonoHandler(clientSocket, commandManager, nameOfUser, inn, outt))

    }


    private fun userChecker(){
        asquirePool()
        while(!registrationStatus){

            val user = inn.readObject() as UserSerialize
            val url = "jdbc:postgresql://localhost:5432/studs"
            val connection = DriverManager.getConnection(url)
            if(user.getTypeOfAuth() == "Registered"){
                checkRegistration(connection, user)
            }
            else {
                registration(connection, user)

            }
        }
        releasePool()
    }


    private fun checkRegistration(connection : Connection, user : UserSerialize){

        try{

            if(checkPerson(connection, user)){

                val saltStatement = connection.prepareStatement("SELECT salt FROM userssalt WHERE name = ?")
                saltStatement.setString(1, user.getName())
                var salt : String
                val saltRs = saltStatement.executeQuery()
                while (saltRs.next()){
                    salt = saltRs.getString("salt")
                    val pass = PasswordHasher.hashPassword(user.getPassword(), salt)

                    val statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE name = ? and password = ?")
                    statement.setString(1, user.getName())
                    statement.setString(2, pass)

                    val rs = statement.executeQuery()

                    while(rs.next()){
                        try{
                            val cnt = rs.getInt("count")
                            if(cnt > 0){
                                println("Пользователь '${user.getName()}' обнаружен")
                                nameOfUser = user.getName()
                                Server.logger.info("Пользователь '${user.getName()}' обнаружен")
                                outt.writeObject(Answer("OK"))
                                outt.flush()

                                registrationStatus = true;
                            }
                            else{
                                println("Пользователь '${user.getName()}' не существует")
                                outt.writeObject(Answer("Пользователь '${user.getName()}' не существует"))
                                Server.logger.info("Пользователь '${user.getName()}' не существует")
                                outt.flush()
                            }
                        }
                        catch(e : NumberFormatException){
                            Server.logger.error("Неверный формат ввода")
                        }
                    }
                }
            }
            else{
                println("Пользователь '${user.getName()}' не существует")
                outt.writeObject(Answer("Пользователь '${user.getName()}' не существует"))
                Server.logger.info("Пользователь '${user.getName()}' не существует")
                outt.flush()
            }

        }catch (e : PSQLException){
            outt.writeObject(Answer("Пользователь '${user.getName()}' не существует"))
            outt.flush();
            Server.logger.error("Пользователь '${user.getName()}' не существует")
        }
    }


    private fun registration(connection: Connection, user: UserSerialize){
        val statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE name = ?")
        statement.setString(1,user.getName())
        val rs = statement.executeQuery()

        while(rs.next()){
            try{
                val cnt = rs.getInt("count")
                if(cnt > 0){
                    println("Пользователь '${user.getName()}' уже существует")
                    outt.writeObject(Answer("Существует"))
                    outt.flush()
                    Server.logger.info("Пользователь '${user.getName()}' уже существует")
                }
                else {
                    val salt = getRandomString(32)
                    val pass = PasswordHasher.hashPassword(user.getPassword(), salt)

                    val st = connection.prepareStatement("INSERT INTO users (name, password) VALUES( ? , ?)")
                    st.setString(1, user.getName())
                    st.setString(2, pass)
                    st.executeUpdate()
                    val saltStatement = connection.prepareStatement("INSERT INTO userssalt (name, salt) VALUES(?, ?)")
                    saltStatement.setString(1, user.getName())
                    saltStatement.setString(2, salt)
                    saltStatement.executeUpdate()
                    println("Пользователь добавлен")
                    println("Регистрация прошла успешно")
                    nameOfUser = user.getName()
                    outt.writeObject(Answer("Регистрация прошла успешно"))
                    outt.flush()
                    registrationStatus = true
                    Server.logger.info("Регистрация прошла успешно")
                }
            }catch (_: PSQLException){
                outt.writeObject(Answer("Регистрация прошла успешно"))
                outt.flush()
            }
        }
    }

    private fun checkPerson(connection : Connection, user : UserSerialize) : Boolean{
        val statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE name = ?")
        statement.setString(1,user.getName())
        val rs = statement.executeQuery()
        var cnt = 0;

        while(rs.next()){

            cnt = rs.getInt("count")
        }

        return cnt > 0
    }

    private fun getRandomString(length : Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        Server.logger.info("Создана рандомная соль")
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")

    }

    private fun asquirePool(){
        semaphore.acquire()
        Server.logger.info("Отрытие доступа для 1 подключения")
    }

    private fun releasePool(){
        semaphore.release()
        Server.logger.info("Убираем подключение")
    }
}