package JavaClasses;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.LocalDate;


public class MusicBand implements Serializable {
    private int id;
    private String name;
    private Coordinates coordinates;
    private Timestamp creationDate;
    private Long numberOfParticipants;
    private String description;
    private MusicGenre genre;
    private Person frontMan;
    private static int counter = 1;

   public MusicBand(String name, Coordinates coordinates, Long numberOfParticipants,
              String description, MusicGenre genre, Person frontMan) {

       this.name = name;
       this.coordinates = coordinates;
       this.creationDate = new Timestamp(System.currentTimeMillis());
       this.numberOfParticipants = numberOfParticipants;
       this.description = description;
       this.genre = genre;
       this.frontMan = frontMan;
       id = counter++;
   }

    public static void setId(int id){

       counter = id;

    }


    public int getId(){

        return id;
    }

    public String getName(){

        return name;

    }

    public String getDescription(){

        return description;

    }

    public Long getNumberOfParticipants(){

        return numberOfParticipants;

    }


    public Person getFrontMan(){

        return frontMan;

    }

    public Coordinates getCoordinates(){
       return coordinates;
    }

    public Timestamp getCreationDate(){
       return creationDate;
    }

    public String getGenre(){
       return genre.toString();
    }


    @Override
    public String toString(){

        return "Id группы : " + id +
                "\nНазвание группы : " + name +
                "\nДата создания элемента : " +  creationDate.toString() +
                "\nКоординаты местоположения группы : " + coordinates.toString() +
                "\nКоличество участников : " + numberOfParticipants +
                "\nОписание группы : " + description +
                "\nМузыкальный жанр : " + genre.toString() +
                "\nСолист : " + frontMan;
    }
}
