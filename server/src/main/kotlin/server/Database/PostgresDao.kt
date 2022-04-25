package server.Database

import JavaClasses.*
import server.Managers.DatabaseConnection
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*


class PostgresDao : Dao{

    private val connection : Connection;
    private val databaseConnection : DatabaseConnection;

    constructor(databaseConnection: DatabaseConnection){

        this.databaseConnection = databaseConnection;
        this.connection = databaseConnection.connectionToDatabase()

    }

    override fun add(musicBand: MusicBand?, owner: String) : Boolean{

        val statement = connection.prepareStatement("INSERT INTO" +
                " musicbands(name, coordx, coordy, creationdate, numberofparticipants, description, genre, nameofperson, height, passportid, locationx, locationy, locationz, owner)" +
                " VALUES(?, ? , ? ,?, ? ,? ,? ,? ,? ,? ,? , ? , ?, ?) ")
        statement.setString(1 , musicBand!!.name)
        statement.setLong(2, musicBand.coordinates.x)
        statement.setDouble(3, musicBand.coordinates.y)
        statement.setTimestamp(4, musicBand.creationDate)
        statement.setLong(5, musicBand.numberOfParticipants)
        statement.setString(6, musicBand.description)
        statement.setString(7, musicBand.genre)
        statement.setString(8, musicBand.frontMan.name)
        statement.setInt(9, musicBand.frontMan.height)
        statement.setString(10, musicBand.frontMan.passportID)
        statement.setLong(11, musicBand.frontMan.location.x)
        statement.setInt(12, musicBand.frontMan.location.y)
        statement.setLong(13, musicBand.frontMan.location.z )
        statement.setString(14, owner)
        val rs = statement.executeUpdate()
        statement.close();
        return rs > 0


    }

    override fun update(id: String, musicBand: MusicBand?, owner : String) : Boolean {

        try {
            val newId = id.toInt()
            var st = connection.prepareStatement("SELECT * FROM musicbands WHERE id = $newId and owner = $owner")
            var result = st.executeUpdate()
            st.close()
            if(result > 0) {
                val statement = connection.prepareStatement(
                    "UPDATE" +
                            " musicbands SET name = ? , coordx = ?, coordy = ?, creationDate = ?, numberofparticipants = ?," +
                            "description = ?, genre = ?, nameofperson = ?, height = ?, passportid = ?, locationx = ?," +
                            " locationy = ?, locationz = ? WHERE id = ? and owner = ?"
                )

                statement.setString(1, musicBand!!.name)
                statement.setLong(2, musicBand.coordinates.x)
                statement.setDouble(3, musicBand.coordinates.y)
                statement.setTimestamp(4, musicBand.creationDate)
                statement.setLong(5, musicBand.numberOfParticipants)
                statement.setString(6, musicBand.description)
                statement.setString(7, musicBand.genre)
                statement.setString(8, musicBand.frontMan.name)
                statement.setInt(9, musicBand.frontMan.height)
                statement.setString(10, musicBand.frontMan.passportID)
                statement.setLong(11, musicBand.frontMan.location.x)
                statement.setInt(12, musicBand.frontMan.location.y)
                statement.setLong(13, musicBand.frontMan.location.z)
                statement.setInt(14, newId)
                val rs = statement.executeUpdate()
                statement.close()
                return rs > 0
            }
            else return false


        }catch (e : NumberFormatException){
            return false
        }
    }

//    override fun show(){
//
//        val rs = statement.executeQuery("SELECT * FROM musicbands")
//
//        while(rs.next()){
//            val musicBand = createMusicBand(rs);
//            println(musicBand.toString())
//        }
//        statement.close()
//
//    }
//
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

    override fun clear(owner: String) : Boolean{

        return try{
            val statement = connection.prepareStatement("DELETE FROM musicbands WHERE id > 0 and owner = $owner")
            var rs = statement.executeUpdate()
            statement.close()
            rs > 0
        }catch (e : SQLException){
            println("404 in clear")
            false
        }


    }
    override fun remove(id: String, owner: String) : Boolean{

        try{
            val newId = id.toInt()
            val statement = connection.prepareStatement("DELETE FROM musicbands WHERE id = $newId and owner = $owner")
            val rs = statement.executeUpdate()
            statement.close()
            return rs > 0

        }
        catch (e : NumberFormatException){
            println("id должен быть числом!")
            return false
        }
        catch (e : SQLException){
            println("404 in remove")
            return false
        }
    }

    override fun removeAllByDescription(description: String, owner: String) : Boolean{

        return try{
            val statement = connection.prepareStatement("DELETE FROM musicbands WHERE description = $description and owner = $owner")
            val rs = statement.executeUpdate()
            statement.close()
            rs > 0
        } catch (e : SQLException){
            println("404 in removeAllByDescription")
            false
        }
    }

//    override fun printlnFrontManDescending() {
//
//        try {
//            val statement = connection.prepareStatement("SELECT nameofperson, height, passportid, locationx, locationy, locationz FROM musicbands")
//            val rs = statement.executeQuery()
//            val listOfPersons = LinkedList<Person>()
//            while (rs.next()) {
//                val nameOfPerson = rs.getString("name")
//                val height = rs.getInt("height")
//                val passportId = rs.getString("passportid")
//                val locationx = rs.getLong("locationx")
//                val locationy = rs.getInt("locationy")
//                val locationz = rs.getLong("locationz")
//                val location = Location(locationx, locationy, locationz)
//                listOfPersons.add(Person(nameOfPerson, height, passportId, location))
//            }
//            listOfPersons.sortByDescending { it.name }
//            listOfPersons.forEach { println(it.toString()) }
//        }
//        catch (e : SQLException){
//            println("404 in printlnFrontManDesc")
//        }
//        statement.close()
//
//    }

//    override fun countLessThan(numberOfParticipants: String) {
//
//        try {
//            val newNumberOfParticipants = numberOfParticipants.toInt();
//            val rs = statement.executeQuery("SELECT COUNT(*) FROM musicbands WHERE numberofparticipants > $newNumberOfParticipants")
//            while(rs.next()){
//                println(rs.getInt("count"))
//            }
//        }
//        catch (e : java.lang.NumberFormatException){
//            println("Количество участников должно быть представлено число!")
//        }
//        catch(e : SQLException){
//            println("404 in countLessThan")
//        }
//        statement.close()
//    }

    override fun removeGreater(name: String, owner: String) : Boolean{

        try {
            val statement = connection.prepareStatement("DELETE FROM musicbands WHERE name > $name and owner = $owner")
            val rs = statement.executeUpdate()
            statement.close()
            return rs > 0
        }catch(e : SQLException) {
            println("404 in removeGreater")
            return false
        }

    }

    override fun removeFirst(owner: String) : Boolean{
        try {
            val statement = connection.prepareStatement("DELETE FROM musicbands WHERE id = 1 and owner = $owner")
            val rs = statement.executeUpdate()
            statement.close()
            return rs > 0
        }catch (e : SQLException){
            println("404 in removeFirst")
            return false
        }
    }


}