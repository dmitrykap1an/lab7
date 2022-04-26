package server.Managers

import server.serverWork.Server
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties
import kotlin.system.exitProcess

class DatabaseConnection {
    private val url : String = "jdbc:postgresql://localhost:1441/studs"
    private val properties : Properties;
    var connection: Connection? = null

    constructor(properties: Properties){
        this.properties = properties
    }

    fun connectionToDatabase(): Connection {

        while (connection == null) {

                try {

                    connection = DriverManager.getConnection(url, properties)
                    println("Подключение к базе данных прошло успешно")

                } catch (e: SQLException) {
                    println("Ошибка подключение к базе данных")
                    closeConnection()
                }


        }
        return connection!!
    }


    private fun closeConnection() {
        if (connection != null) {
            try {
                connection!!.close()
                println("Соединение с базой данных разорвано.")
                Server.logger.info("Соединение с базой данных разорвано.")
            } catch (exception: SQLException) {
                println("Произошла ошибка при разрыве соединения с базой данных!")
                Server.logger.error("Произошла ошибка при разрыве соединения с базой данных!")
                exitProcess(0)
            }
        }
        else{
            println("Закрытие сервера")
                exitProcess(0)
        }
    }


}
