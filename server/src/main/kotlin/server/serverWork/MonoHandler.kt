package server.serverWork

import general.AppIO.CommandSerialize
import server.Managers.CommandManager
import java.io.*
import java.net.SocketException
import java.nio.channels.SocketChannel
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantReadWriteLock

class MonoHandler : Runnable{
    private val clientSocket : SocketChannel;
    private val commandManager : CommandManager;
    private val nameOfUser : String
    private val inn : ObjectInputStream
    private val outt : ObjectOutputStream
    private val writeLock = ReentrantReadWriteLock()
    private val semaphore = Semaphore(1, false)

    constructor(clientSocket : SocketChannel, commandManager: CommandManager, nameOfUser : String, inn : ObjectInputStream, outt : ObjectOutputStream){
        this.clientSocket = clientSocket;
        this.commandManager = commandManager;
        this.nameOfUser = nameOfUser
        this.inn = inn
        this.outt = outt
    }

    override fun run() {
        try{
            while (true) {
                if(Thread.currentThread().isInterrupted) break
                requestToClient()
            }
        }
        catch (e : IOException){
            e.printStackTrace()
            println("Клиент $nameOfUser отключился")
            Thread.currentThread().interrupt()
        }
    }

    private fun requestToClient() {

        try {
            val command = inn.readObject() as CommandSerialize
            println("Команда ${command.getNameCommand()} принята от $nameOfUser")
            Server.logger.info("Команда принята")
            commandManager.addToHistory(command.getNameCommand())
            val answer = commandManager.launchCommand(command, nameOfUser)
            outt.writeObject(answer)
        } catch (e: EOFException) {
            println("Ошибка конца ввода")
            println("Ожидание 5 секунд")
            Thread.sleep(5000L)

        } catch (e: SocketException) {
            println("Ошибка соединения")

        } catch (e: ClassCastException) {
            println("Ошибка исполнения")
        }
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