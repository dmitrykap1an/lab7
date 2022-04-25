package server.serverWork

import general.AppIO.Answer
import general.Exceptions.CloseSocketException
import org.apache.logging.log4j.LogManager
import server.Managers.CommandManager
import java.io.*
import java.net.*
import java.nio.ByteBuffer
import java.nio.channels.NotYetBoundException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore


class Server(commandManager: CommandManager, port : Int, maxClients : Int) : Runnable{

    private lateinit var clientSocket : SocketChannel//сокет для общения
    private var commandManager : CommandManager = commandManager
    private  val PORT : Int = port
    private var server : ServerSocketChannel? = null// серверный сокет
    private val semaphore = Semaphore(2)
    private val BUFFERSIZE = 1024
    private lateinit var selector: Selector;
    private val executor = Executors.newFixedThreadPool(10);
        companion object{
        internal val logger  = LogManager.getLogger(Server::class)

    }

    @Synchronized
    override fun run() {
        openServer()


        while (true) {
            try {
                    asquirePool()
                    connectToClient()
                    executor.submit(ConnectionRequest(clientSocket, commandManager))
            } catch (e: SocketException) {
                logger.error("Потеряно соединение")
                println("Соединение потеряно")
            } catch (e: UninitializedPropertyAccessException) {
                println("Клиентский сокет не был создан")
            } catch (e: NullPointerException) {
                println("Соединение с клиентом не было создано")
            }


        }
    }

    private fun asquirePool(){
        semaphore.acquire()
        logger.info("Отрытие доступа для 1 подключения")
    }

    private fun releasePool(){
        semaphore.release()
        logger.info("Убираем подключение")
    }


    private fun serverStop() {
        try {
            logger.info("Завершение работы сервера...")
            println("Завершение работы сервера...")
            if (server == null) throw CloseSocketException()
            clientSocket.close()
//            server!!.close()
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
            server = ServerSocketChannel.open()
            server!!.socket().bind(InetSocketAddress(4004))
            logger.info("Сервер успешно запущен.")
            println("Сервер успешно запущен.")
        } catch (e: IOException) {
            logger.fatal("Произошла ошибка при попытке использовать порт '$PORT'!")
            println("Произошла ошибка при попытке использовать порт '$PORT'!")
        }
        catch (e : NullPointerException){
            println("Ошибка соединения")
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
        catch(e : NotYetBoundException){
            println("Канал занят")
        }
    }

}