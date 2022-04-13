package client

import client.Managers.AuthManager
import client.Managers.CommandFinder
import general.Exceptions.CommandException
import java.io.*
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import kotlin.system.exitProcess

class Client(commandFinder : CommandFinder, port : Int, host : String) : Runnable{


    private lateinit var clientSocket: Socket;//сокет для общения
    private lateinit var inn: BufferedReader; // поток чтения из сокета
    private lateinit var outt: ObjectOutputStream; // поток записи в сокет
    private val commandFinder : CommandFinder = commandFinder;
    private val PORT = port;
    private val HOST = host;
    private var registrationStatus = false;


    override fun run(){
        println("Клиент запущен")
        while(true) {
            try {
                requestToServer()
            } catch (e: UninitializedPropertyAccessException) {
                println("Сервер не доступен")
                println("Время ожидания 5 секунд")
                Thread.sleep(5000L)
            }
        }
    }


    private fun registration(){

        while(!registrationStatus) {
            val user = AuthManager.handle()
            outt.writeObject(user)
            registrationStatus = true
            val status = inn.readLines()
            if (status.isNotEmpty() && status[0] == "OK"){
                registrationStatus = true;
                println("Вход произошел успешно")
            }
            else if(status[0].split(' ')[0] == "Пользователь"){
                println(status[0])
            }
            else println("Пользователь с такими именем и паролем не обнаружен")
        }
    }

    private fun requestToServer(){
        try {
            clientSocket = Socket( HOST, PORT) // запрашиваем у сервера доступ на соединение
            inn = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            outt = ObjectOutputStream(clientSocket.getOutputStream())
            registration()
            val command = commandFinder.commandSearcher() ?: throw CommandException()
            outt.writeObject(command) // отправляем сообщение на сервер
            outt.flush()
            val serverWords = inn.readLines() // ждём, что скажет сервер
            serverWords.forEach { println(it) } // получив - выводим на экран
            if(command.getNameCommand() == "exit"){
                exitProcess(0)
            }

        } catch (e: ConnectException) {
            println("Связь нарушена")
        } catch (e : CommandException){
            println("Команда не найдена")
        }catch (e : EOFException){
            println("Ошибка конца ввода")
        }catch (e : SocketException) {
            println("Сервер закрыт")
        }finally {
            clientSocket.close()
            inn.close()
            outt.close()
        }
    }
}