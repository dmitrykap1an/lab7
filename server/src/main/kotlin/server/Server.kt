package server

import client.Managers.*
import client.User
import general.AppIO.CommandSerialize
import general.Exceptions.CloseSocketException
import org.apache.logging.log4j.LogManager
import org.postgresql.util.PSQLException
import server.Database.PasswordHasher
import server.Managers.CommandManager
import java.io.*
import java.net.*
import java.sql.*
import kotlin.random.Random


class Server(commandManager: CommandManager, port : Int, soTimeOut : Int) : Runnable{

    private lateinit var clientSocket : Socket//сокет для общения
    private var commandManager : CommandManager = commandManager
    private  val PORT : Int = port
    private val soTimeOut : Int = soTimeOut;
    private var server : ServerSocket? = null// серверный сокет
    private var registrationStatus = false
    companion object{
        internal val logger  = LogManager.getLogger(Server::class)
        internal lateinit var inn : ObjectInputStream;// поток чтения из сокета
        internal lateinit var outt : BufferedWriter;// поток записи в сокет
    }

    override fun run() {

        try {
            openServer()

            while (true) {
                connectToClient()
                requestToClient()

            }

        }catch (e : SocketException){
            logger.error("Потеряно соединение")
        }catch (e : UninitializedPropertyAccessException){
            println("Клиентский сокет не был создан")
        } finally{
            serverStop()
        }
    }

    private fun serverStop() {
        try {
            logger.info("Завершение работы сервера...")
            println("Завершение работы сервера...")
            if (server == null) throw CloseSocketException()
            server!!.close()
            println("Работа сервера успешно завершена.")
            logger.info("Работа сервера успешно завершена.")
        } catch (e : CloseSocketException) {
            println("Невозможно завершить работу сервера : сервер изначально был закрыт!")
            logger.error("Невозможно завершить работу сервера : сервер изначально был закрыт!")
        } catch (e : IOException) {
            println("Произошла ошибка при завершении работы сервера!")
            logger.error("Произошла ошибка при завершении работы сервера!")
        }
    }

    private fun openServer() {
        try {
            logger.info("Запуск сервера...")
            println("Запуск сервера...")
            server = ServerSocket(PORT)
            server!!.soTimeout = soTimeOut
            logger.info("Сервер успешно запущен.")
            println("Сервер успешно запущен.")
        } catch (e: IOException) {
            logger.fatal("Произошла ошибка при попытке использовать порт '$PORT'!")
            println("Произошла ошибка при попытке использовать порт '$PORT'!")
        }
    }

    /**
     * Connecting to client.
     */
    private fun connectToClient(){
        try {
            logger.info("Прослушивание порта '$PORT'...")
            clientSocket = server!!.accept()
            logger.info("Соединение с клиентом успешно установлено.")
            println("Соединение с клиентом успешно установлено.")

        } catch (e : SocketTimeoutException) {
            logger.warn("Превышено время ожидания подключения!")
            println("Превышено время ожидания подключения!")
        } catch (e : IOException){
            logger.error("Произошла ошибка при соединении с клиентом!")
            println("Произошла ошибка при соединении с клиентом!")
        }catch (e : NullPointerException){
            println("Соединение с клиентом не установлено")
        }
    }

    private fun requestToClient(){
        try {

            outt = BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream()))
            inn = ObjectInputStream(clientSocket.getInputStream())
            userChecker()
            val command = inn.readObject() as CommandSerialize;
            logger.info("Команда принята")
            commandManager.addToHistory(command.getNameCommand())
            commandManager.launchCommand(command)

        } catch (_: EOFException) {

        }catch (_: SocketException){

        } finally {
            clientSocket.close()
            inn.close()
            outt.close()
        }
    }


    private fun userChecker(){

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
    }


    private fun checkRegistration(connection : Connection, user : UserSerialize){

        try{

            if(checkPerson(connection, user)){

                println("try to find salt")
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
                            println(cnt)
                            if(cnt > 0){
                                println("Пользователь '${user.getName()}' обнаружен")
                                Server.logger.info("Пользователь '${user.getName()}' обнаружен")
                                outt.write("OK")
                                outt.newLine()
                                outt.flush()
                                registrationStatus = true;
                            }
                            else{
                                println("Пользователь '${user.getName()}' не существует")
                                outt.write("Пользователь '${user.getName()}' не существует")
                                Server.logger.info("Пользователь '${user.getName()}' не существует")
                                outt.newLine()
                                outt.flush()
                            }
                        }
                            catch(e : NumberFormatException){
                            logger.error("Неверный формат ввода")
                        }
                    }
                }
            }
            else{
                println("Пользователь '${user.getName()}' не существует")
                outt.write("Пользователь '${user.getName()}' не существует")
                logger.info("Пользователь '${user.getName()}' не существует")
                outt.newLine()
                outt.flush()
            }

        }catch (e : PSQLException){
            outt.write("Пользователь '${user.getName()}' не существует")
            outt.flush();
            logger.error("Пользователь '${user.getName()}' не существует")
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
                    outt.write("Существует")
                    outt.newLine()
                    outt.flush()
                    logger.info("Пользователь '${user.getName()}' уже существует")
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
                    outt.write("Регистрация прошла успешно")
                    outt.newLine()
                    outt.flush()
                    registrationStatus = true
                    logger.info("Регистрация прошла успешно")
                }
            }catch (_: PSQLException){
                outt.write("Регистрация прошла успешно")
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
        logger.info("Создана рандомная соль")
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")

    }

}




