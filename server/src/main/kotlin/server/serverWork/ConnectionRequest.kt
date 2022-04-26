package server.serverWork

import client.Managers.UserSerialize
import general.AppIO.Answer
import org.postgresql.util.PSQLException
import server.Database.PasswordHasher
import server.Managers.CommandManager
import java.io.FileReader
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.channels.*
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

class ConnectionRequest : Runnable {
    private var registrationStatus = false
    private var clientSocket : SocketChannel
    private val commandManager : CommandManager
    private lateinit var nameOfUser : String;
    private val executor : ExecutorService
    private val semaphore = Semaphore(1, false)
    private lateinit var outt: ObjectOutputStream
    private lateinit var inn: ObjectInputStream

    constructor(clientSocket : SocketChannel, commandManager: CommandManager, executor : ExecutorService){
        this.clientSocket = clientSocket
        this.commandManager = commandManager
        this.executor = executor
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

            val properties = Properties()
            val file = FileReader("/home/newton/IdeaProjects/lab6-master/server/src/main/kotlin/server/databaseInfo.properties")
            properties.load(file)
            val future = executor.submit(ObjectReader<UserSerialize>(inn))
            val user = future.get()
            val url = "jdbc:postgresql://localhost:1441/studs"
            val connection = DriverManager.getConnection(url, properties)
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
                                val answer = Answer("OK")
                                val future = executor.submit(ObjectWriter(outt, answer))
                                val result = future.get()
                                registrationStatus = true;
                            }
                            else{
                                println("Пользователь '${user.getName()}' не существует")
                                val answer = Answer("Пользователь '${user.getName()}' не существует")
                                val future = executor.submit(ObjectWriter(outt, answer))
                                val result = future.get()
                                Server.logger.info("Пользователь '${user.getName()}' не существует")
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
                val answer = Answer("Пользователь '${user.getName()}' не существует")
                val future = executor.submit(ObjectWriter(outt, answer))
                val result = future.get()
                Server.logger.info("Пользователь '${user.getName()}' не существует")
            }

        }catch (e : PSQLException){
            val answer = Answer("Пользователь '${user.getName()}' не существует")
            val future = executor.submit(ObjectWriter(outt, answer))
            val result = future.get()
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
                    val answer = Answer("Существует")
                    val future = executor.submit(ObjectWriter(outt, answer))
                    val result = future.get()
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
                    val answer = Answer("Регистрация прошла успешно")
                    val future = executor.submit(ObjectWriter(outt, answer))
                    val result = future.get()
                    registrationStatus = true
                    Server.logger.info("Регистрация прошла успешно")
                }
            }catch (_: PSQLException){
                val answer = Answer("Регистрация прошла успешно")
                val future = executor.submit(ObjectWriter(outt, answer))
                val result = future.get()
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