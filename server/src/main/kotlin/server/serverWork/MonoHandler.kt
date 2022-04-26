package server.serverWork

import general.AppIO.CommandSerialize
import server.Managers.CommandManager
import java.io.*
import java.net.SocketException
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.locks.ReentrantReadWriteLock

class MonoHandler : Runnable{
    private val clientSocket : SocketChannel;
    private val commandManager : CommandManager;
    private val nameOfUser : String
    private val inn : ObjectInputStream
    private val outt : ObjectOutputStream
    private val executor = Executors.newFixedThreadPool(1);

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
            println("Клиент $nameOfUser отключился")
            Thread.currentThread().interrupt()
        }
        catch (e : EOFException){
            println("Клиент $nameOfUser отключился")
            Thread.currentThread().interrupt()
        }
    }

    private fun requestToClient() {

        try {

            val futureOne = executor.submit(ObjectReader<CommandSerialize>(inn))
            val command = futureOne.get()
            println("Команда ${command.getNameCommand()} принята от $nameOfUser")
            Server.logger.info("Команда ${command.getNameCommand()} принята от $nameOfUser")
            commandManager.addToHistory(command.getNameCommand(), nameOfUser)
            val future = executor.submit(CollectionRequester(commandManager, command, nameOfUser))
            val answer = future.get()
            val futureTwo = executor.submit(ObjectWriter(outt, answer))
            val result = futureTwo.get()
        } catch (e: EOFException) {
            println("Клиент $nameOfUser отключился")
            Thread.currentThread().interrupt()

        } catch (e: SocketException) {
            println("Ошибка соединения")
        } catch (e: ClassCastException) {
            println("Ошибка исполнения")
        }
    }

}