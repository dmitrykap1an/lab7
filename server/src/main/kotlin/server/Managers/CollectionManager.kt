package server.Managers

import JavaClasses.MusicBand
import client.Managers.FileManager
import general.AppIO.InputData
import general.Exceptions.EmptyArgumentException
import general.AppIO.Answer
import server.serverWork.Server
import java.time.LocalDateTime
import java.util.*

/**
 * Класс для работы с коллекцией музыкальных групп
 * @param lastSaveTime время последнего сохранения коллекции в файл
 * @param lastInitTime время последнего создания элемента коллекции
 */
class CollectionManager {

    private var collectionOfMusicBands: MutableList<MusicBand> = LinkedList()
    private lateinit var lastSaveTime: LocalDateTime;
    private lateinit var lastInitTime: LocalDateTime;
    private val fileManager: FileManager;
    private val inputData : InputData;

    constructor(fileManager: FileManager, inputData: InputData){
        this.fileManager = fileManager;
        this.inputData = inputData
    }

//
//    init{
//        addFileCommandsToCollection()
//    }

    fun update(id: String, musicBand: MusicBand?, list: List<String> = listOf()) : Answer {

        try {

            val newId = id.toInt()
            if(newId > collectionOfMusicBands.size || newId < 0){


            } else {
                for (i in collectionOfMusicBands.indices) {

                    if (collectionOfMusicBands[i].id == newId) {

                        if (list.isEmpty()) {

                            collectionOfMusicBands[i] = musicBand!!;
                            break;

                        } else {

                            val localParam = inputData.askMusicBand(list)
                            if (localParam != null)
                                collectionOfMusicBands[i] = localParam;
                        };

                        collectionOfMusicBands.stream().sorted(compareBy { it.name })

                    }
                }

            }
        } catch (e : NumberFormatException) {
            return Answer("Данная строка не является числом")
        }catch (e : IllegalArgumentException) {
            return Answer("Данная строка не является числом")
        }catch (e : NullPointerException){
                return Answer("Ошибка : Команда не выполнена")
        }
        return Answer("Команда update выполнена")
    }

//    private fun addFileCommandsToCollection(){
//
//
//        val collection = fileManager.collectionReader("bootData");
//        var mx = 0;
//        collection.forEach {
//            mx = max(it.id, mx)
//            collectionOfMusicBands.add(it)
//        }
//        MusicBand.setId(mx + 1)
//        collectionOfMusicBands.stream().sorted(compareBy { it.name });
//        lastInitTime = LocalDateTime.now();
//
//
//    }

    fun info() : Answer {

        Server.logger.info("Выполнение команды info")
        try {
                return Answer("Date of init : $lastInitTime\n" +
                        "Date of last save : $lastSaveTime\n" +
                        "Type : ${collectionOfMusicBands::class.simpleName}\n" +
                        "Count of elements : ${collectionOfMusicBands.size}\n")


        }
        catch (e : UninitializedPropertyAccessException){

            try{
                   return Answer("Date of init : $lastInitTime\n" +
                            "Date of last save : the collection wasn't saved to a file\n" +
                            "Type : ${collectionOfMusicBands::class.simpleName}\n" +
                            "Count of elements : ${collectionOfMusicBands.size}\n")


            }
            catch (e : UninitializedPropertyAccessException){


                   return Answer("Date of init : the collection is empty\n" +
                            "Date of last save : the collection wasn't saved to a file\n" +
                            "Type : ${collectionOfMusicBands::class.simpleName}\n" +
                            "Count of elements : ${collectionOfMusicBands.size}\n")
            }

        }
    }

    fun show() : Answer {

        val result = StringBuilder()
        Server.logger.info("Выполнение команды show")
        if (collectionOfMusicBands.size > 0) {
            collectionOfMusicBands.stream().forEach {
                result.append(it.toString() + "\n")
            }

            return Answer(result.toString())
        }
        else {
            return Answer("Коллекция пустая")
        }


    }

    fun add(command : MusicBand?, list : List<String> = listOf()) : Answer {

        Server.logger.info("Выполнение команды add")
        val musicBand : MusicBand? = command ?: inputData.askMusicBand(list);

        if(musicBand != null)
            collectionOfMusicBands.add(musicBand);
        collectionOfMusicBands.stream().sorted(compareBy { it.name })
        lastInitTime = LocalDateTime.now();

        return Answer("Команда add выполнена")

    }

    fun clear() : Answer {

        Server.logger.info("Выполнение команды clear")
        collectionOfMusicBands.clear();
        return Answer("Коллекция очищена")

    }

    fun remove(id : String) : Answer {

        Server.logger.info("Выполнение команды remove_by_id ")
        try {

            val newId = id.toInt()
            if(newId <= collectionOfMusicBands.size) {

                for (i in collectionOfMusicBands.indices) {

                    if (collectionOfMusicBands[i].id == newId) {

                        collectionOfMusicBands.remove(collectionOfMusicBands[i])
                        break;
                    }

                    if(i == collectionOfMusicBands.size - 1) return Answer("Id не найден")
                }
            } else return Answer("Id не найден")

        } catch (e : NumberFormatException){
            return Answer("Данная строка не является числом")
        }catch (e : IllegalArgumentException){
            return Answer("Данная строка не является числом")
        }
        return Answer("Элемент удален")

    }

    fun save() : Answer {

        Server.logger.info("Выполнение команды save")
        fileManager.collectionWriter(collectionOfMusicBands);
        lastSaveTime = LocalDateTime.now();
        return Answer("Коллекция сохранена")


    }


    fun removeFirst() : Answer {

        Server.logger.info("Выполнение команды remove_first")
        if(collectionOfMusicBands.size > 0) {

            collectionOfMusicBands.remove(collectionOfMusicBands[0]);
            return Answer("Первый элемент удален")
        }
        else {
            return Answer("Невозможно удалить первый элемент - коллекция пуста")
        }

    }

    fun removeAllByDescription(description : String) : Answer {

        Server.logger.info("Выполнение команды remove_all_by_description")
        var cnt = 0;

        for (i in collectionOfMusicBands.indices) {

            if (collectionOfMusicBands[i].description.equals(description)) {

                collectionOfMusicBands.remove(collectionOfMusicBands[i]);
                cnt++

            }
        }

        return when {

            cnt == 0 -> Answer("Элемент по описанию не найден")

            cnt == 1 -> Answer("Элемент удален")

            cnt >= 2 -> Answer("Элементы удалены")

            else -> Answer("Элемент по описанию не найден")

        }
    }


    fun removeGreater(name : String) : Answer {

        Server.logger.info("Выполнение команды remove_greater")
        var message : String = ""
        try {

            if(collectionOfMusicBands.isEmpty()) throw EmptyArgumentException()
            for(i in collectionOfMusicBands.indices) {


                if (collectionOfMusicBands[i].name == name) {

                    collectionOfMusicBands = collectionOfMusicBands.subList(0, i + 1);
                    message = ("Элемент(ы) удален(ы)")
                    break;
                }

                if(i == collectionOfMusicBands.size - 1) message = "Элемент не найден"
            }

        }catch (e : EmptyArgumentException) {
            return Answer("Имя не найдено")
        }

        return Answer(message)
    }



    fun countLessThan(numberOfParticipants : String) : Answer {

        Server.logger.info("Выполнение команды count_less_than_number_of_participants numberOfParticipants")
        try {

            val newNumber = numberOfParticipants.toLong();
            var cnt = 0;
            for(i in collectionOfMusicBands.indices){

                if(collectionOfMusicBands[i].numberOfParticipants < newNumber){

                    cnt++;
                }
            }
            return Answer(cnt.toString())
        }catch (e : NumberFormatException){
            return Answer("Данная строка не является числом")
        }catch (e : IllegalArgumentException){
            return Answer("Данная строка не является числом")
        }
    }

    fun printlnFrontManDescending() : Answer {

        val result = StringBuilder()
        Server.logger.info("Выполнение команды print_field_descending_front_man")
        val newCollection = collectionOfMusicBands.asReversed();
        for(i in newCollection.indices){

            result.append(newCollection[i].frontMan.toString() + "\n")

        }

        return Answer(result.toString())
    }


}