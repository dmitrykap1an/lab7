package server.Managers

import server.serverWork.Server
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.Properties

class DatabaseConnection {
    private val url : String = "jdbc:postgresql://localhost:5432/studs"
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
                    println(e.message)
                    println("Ошибка подключение к базе данных")
                }


        }
        return connection!!
    }


    fun closeConnection() {
        if (connection != null) {
            try {
                connection!!.close()
                println("Соединение с базой данных разорвано.")
                Server.logger.info("Соединение с базой данных разорвано.")
            } catch (exception: SQLException) {
                println("Произошла ошибка при разрыве соединения с базой данных!")
                Server.logger.error("Произошла ошибка при разрыве соединения с базой данных!")
            }
        }
    }


}
