package server.Managers

import server.Server
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseConnection {
    private val url : String = "jdbc:postgresql://localhost:5432/studs"
    val user : String;
    val password : String;
    var connection: Connection? = null

    constructor(user : String, password : String){
        this.user = user;
        this.password = password;
    }

    fun connectionToDatabase(): Connection {

        while (connection == null) {

                try {

                    connection = DriverManager.getConnection(url, user, password);
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
