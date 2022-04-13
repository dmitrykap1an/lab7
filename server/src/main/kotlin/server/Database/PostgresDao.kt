package server.Database

import JavaClasses.*
import server.Managers.DatabaseConnection
import java.sql.*
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.LinkedList


class PostgresDao : Dao {

    private val connection : Connection;
    private val databaseConnection : DatabaseConnection;
    private val statement : Statement;

    constructor(databaseConnection: DatabaseConnection){

        this.databaseConnection = databaseConnection;
        this.connection = databaseConnection.connectionToDatabase()
        this.statement = connection.createStatement();

    }

    override fun add(musicBand: MusicBand?) {

        val rs = statement.executeUpdate("INSERT INTO musicbands VALUES(" +
                "${musicBand!!.name}, ${musicBand.coordinates.x}," +
                "${musicBand.coordinates.y}, ${musicBand.creationDate}," +
                "${musicBand.numberOfParticipants}, ${musicBand.description}," +
                "${musicBand.genre}, ${musicBand.frontMan.name}, ${musicBand.frontMan.height}," +
                "${musicBand.frontMan.passportID}, ${musicBand.frontMan.location.x}," +
                "${musicBand.frontMan.location.y}, ${musicBand.frontMan.location.z} ) ");

        statement.close();
    }

    override fun update(id: String, musicBand: MusicBand?) {

        val rs = statement.executeQuery("UPDATE  musicbands SET name = ${musicBand!!.name}" +
                " coordx = ${musicBand.coordinates.x}, coordy = ${musicBand.coordinates.y}" +
                " creationDate = ${musicBand.creationDate}, numberofparticipants = ${musicBand.numberOfParticipants}," +
                " description = ${musicBand.description}, genre = ${musicBand.genre.toString()}, " +
                " nameofperson = ${musicBand.frontMan.name}, height = ${musicBand.frontMan.height}," +
                " passportid = ${musicBand.frontMan.passportID}, locationx = ${musicBand.frontMan.location.x}," +
                " locationy = ${musicBand.frontMan.location.y}, locationz = ${musicBand.frontMan.location.z}" +
                " WHERE id = ${id.toInt()}")
    }

    override fun show(){

        val rs = statement.executeQuery("SELECT * FROM musicbands")

        while(rs.next()){
            val musicBand = createMusicBand(rs);
            println(musicBand.toString())
        }
        statement.close()

    }

    private fun createMusicBand(resultSet: ResultSet): MusicBand {
        val id: Int = resultSet.getInt("id")
        val name: String = resultSet.getString("name")
        val coordinates: Coordinates = Coordinates(resultSet.getLong("coordx"), resultSet.getDouble("coordy"))
        val creationDate: LocalDateTime =
            resultSet.getTimestamp("creationDate").toLocalDateTime()
        val numberOfParticipants = resultSet.getLong("numberofparticipants")
        val description = resultSet.getString("description")
        val genre = resultSet.getString("genre")
        val nameOfPerson = resultSet.getString("nameofperson")
        val height = resultSet.getInt("height")
        val passportId = resultSet.getString("passportid")
        val locationX = resultSet.getLong("locationx")
        val locationY = resultSet.getInt("locationy")
        val locationZ = resultSet.getLong("locationz")
        return MusicBand(
            name,
            coordinates,
            numberOfParticipants,
            description,
            MusicGenre.valueOf(genre),
            Person(nameOfPerson, height, passportId, Location(locationX, locationY, locationZ))

        )
    }

    override fun clear(){

        val rs = statement.executeQuery("DELETE FROM musicbands WHERE id > 0")
        statement.close()
    }

    override fun remove(id: String) {

        try{
            val newId = id.toString()
            val rs = statement.executeQuery("DELETE FROM musicbands WHERE id = $newId")
            statement.close()
        }
        catch (e : NumberFormatException){
            println("id должен быть числом!")
        }
        catch (e : SQLException){
            println("404 in remove")
        }
    }

    override fun removeAllByDescription(description: String) {

        try{
            val rs = statement.executeQuery("DELETE FROM musicbands WHERE description = $description")
        }
        catch (e : SQLException){
            println("404 in removeAllByDescription")
        }
    }

    override fun printlnFrontManDescending() {

        try {
            val rs =
                statement.executeQuery("SELECT nameofperson, height, passportid, locationx, locationy, locationz FROM musicbands")
            var listOfPersons = LinkedList<Person>()
            while (rs.next()) {
                val nameOfPerson = rs.getString("name")
                val height = rs.getInt("height")
                val passportId = rs.getString("passportid")
                val locationx = rs.getLong("locationx")
                val locationy = rs.getInt("locationy")
                val locationz = rs.getLong("locationz")
                val location = Location(locationx, locationy, locationz)
                listOfPersons.add(Person(nameOfPerson, height, passportId, location))
            }
            listOfPersons.sortByDescending { it.name }
            listOfPersons.forEach { println(it.toString()) }
        }
        catch (e : SQLException){
            println("404 in printlnFrontManDesc")
        }
        statement.close()

    }

    override fun countLessThan(numberOfParticipants: String) {

        try {
            val newNumberOfParticipants = numberOfParticipants.toInt();
            val rs = statement.executeQuery("SELECT COUNT(*) FROM musicbands WHERE numberofparticipants > $newNumberOfParticipants")
            while(rs.next()){
                println(rs.getInt("count"))
            }
        }
        catch (e : java.lang.NumberFormatException){
            println("Количество участников должно быть представлено число!")
        }
        catch(e : SQLException){
            println("404 in countLessThan")
        }
        statement.close()
    }

    override fun removeGreater(name: String) {

        try {

            val rs = statement.executeQuery("DELETE FROM musicbands WHERE name > $name")
        }catch(e : SQLException) {
            println("404 in removeGreater")
        }
        statement.close()
    }

    override fun removeFirst() {
        try {
            val rs = statement.executeQuery("DELETE FROM musicbands WHERE id = 1")
        }catch (e : SQLException){
            println("404 in removeFirst")
        }
    }


}