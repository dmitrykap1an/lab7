package server

import client.Managers.UserSerialize
import general.AppIO.CommandSerialize
import general.Exceptions.CloseSocketException
import org.apache.logging.log4j.LogManager
import org.postgresql.util.PSQLException
import server.Managers.CommandManager
import java.io.*
import java.net.*
import java.sql.DriverManager


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
            val statement = connection.createStatement()
            if(user.getTypeOfAuth() == "Registered"){
                try{
                    val rs = statement.executeQuery("SELECT COUNT(*) FROM users" +
                            " WHERE name = '${user.getName()}' and password = '${user.getPassword()}';")

                    while(rs.next()){
                        try{
                            val cnt = rs.getInt("count")
                            if(cnt > 0){
                                println("Пользователь '${user.getName()}' обнаружен")
                                outt.write("OK")
                                outt.flush()
                                registrationStatus = true;
                            }
                            else{
                                println("Пользователь '${user.getName()}' не существует")
                                outt.write("Пользователь '${user.getName()}' не существует")
                                outt.flush()
                            }
                        }
                        catch(e : NumberFormatException){
                            println(e.message)
                        }
                    }
                    rs.close(
                    )
                }catch (e : PSQLException){
                    outt.write("Пользователь '${user.getName()}' не существует")
                    outt.flush();
                }

            }

            else if(user.getTypeOfAuth() == "NotRegistered"){
                try {
                    val rs =
                        statement.executeQuery("INSERT INTO users (name, password) VALUES('${user.getName()}', '${user.getPassword()}');")
                    println("Регистрация прошла успешно")
                    outt.write("Регистрация прошла успешно")
                    outt.flush()
                    registrationStatus = true
                    rs.close()
                }catch (e : PSQLException){
                    outt.write("Регистрация прошла успешно")
                    outt.flush()
                    registrationStatus = true
                }

            }
        }
    }
}



