package server.Managers

import JavaClasses.MusicBand
import general.AppIO.InputData
import general.Exceptions.EmptyArgumentException
import general.AppIO.Answer
import server.Database.PostgresDao
import server.serverWork.Server
import java.lang.Math.max
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
    private val inputData : InputData;
    private val postgresDao : PostgresDao


    constructor(inputData: InputData, postgresDao: PostgresDao){

        this.inputData = inputData
        this.postgresDao = postgresDao
        addMusicBandsToCollection();

    }

//
//    init{
//        addFileCommandsToCollection()
//    }

    fun update(id: String, musicBand: MusicBand?, list: List<String> = listOf(), owner : String) : Answer {

        try {

            if(postgresDao.update(id, musicBand, owner)){
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
            }
            else return Answer("Не удалось обновавить элемент")
        } catch (e : NumberFormatException) {
            return Answer("Данная строка не является числом")
        }catch (e : IllegalArgumentException) {
            return Answer("Данная строка не является числом")
        }catch (e : NullPointerException){
                return Answer("Ошибка : Команда не выполнена")
        }
        return Answer("Команда update выполнена")
    }

    private fun addMusicBandsToCollection(){


        val collection = postgresDao.addMusicBandsToCollection()
        var mx = 0;
        collection.forEach {
            mx = max(it.id, mx)
            collectionOfMusicBands.add(it)
        }
        MusicBand.setId(mx + 1)
        collectionOfMusicBands.stream().sorted(compareBy { it.name });
        lastInitTime = LocalDateTime.now();


    }

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
        return if (collectionOfMusicBands.size > 0) {
            collectionOfMusicBands.stream().forEach {
                result.append(it.toString() + "\n")
            }

            Answer(result.toString())
        } else {
            Answer("Коллекция пустая")
        }


    }

    fun add(command : MusicBand?, list : List<String> = listOf(), owner: String) : Answer {

        Server.logger.info("Выполнение команды add")
        val musicBand : MusicBand? = command ?: inputData.askMusicBand(list);
        return if(postgresDao.add(musicBand, owner)) {
            if (musicBand != null)
                collectionOfMusicBands.add(musicBand);
                collectionOfMusicBands.stream().sorted(compareBy { it.name })
                lastInitTime = LocalDateTime.now();

            Answer("Команда add выполнена")
        } else Answer("Ошибка добавления музыльной группы")

    }

    fun clear(owner : String) : Answer {

        if(postgresDao.clear(owner)) {
            Server.logger.info("Выполнение команды clear")
            collectionOfMusicBands.clear();
            return Answer("Коллекция очищена")
        }
        else return Answer("Ошибка удаления элементов")

    }

    fun remove(id : String, owner: String) : Answer {

        try {
            if(postgresDao.remove(id, owner)) {
                Server.logger.info("Выполнение команды remove_by_id ")
                val newId = id.toInt()
                if (newId <= collectionOfMusicBands.size) {

                    for (i in collectionOfMusicBands.indices) {

                        if (collectionOfMusicBands[i].id == newId) {

                            collectionOfMusicBands.remove(collectionOfMusicBands[i])
                            break;
                        }

                        if (i == collectionOfMusicBands.size - 1) return Answer("Id не найден")
                    }
                } else return Answer("Id не найден")
            }else return Answer("Ошибка удаления элемента по Id")

        } catch (e : NumberFormatException){
            return Answer("Данная строка не является числом")
        }catch (e : IllegalArgumentException){
            return Answer("Данная строка не является числом")
        }
        return Answer("Элемент удален")

    }

//    fun save() : Answer {
//
//        Server.logger.info("Выполнение команды save")
//        fileManager.collectionWriter(collectionOfMusicBands);
//        lastSaveTime = LocalDateTime.now();
//        return Answer("Коллекция сохранена")
//
//
//    }


    fun removeFirst(owner: String) : Answer {

        return if(postgresDao.removeFirst(owner)){
            Server.logger.info("Выполнение команды remove_first")
            if(collectionOfMusicBands.size > 0) {

                collectionOfMusicBands.remove(collectionOfMusicBands[0]);
                Answer("Первый элемент удален")
            } else {
                Answer("Невозможно удалить первый элемент - коллекция пуста")
            }
        } else Answer("Удаление первого элемента невозможно")

    }

    fun removeAllByDescription(description : String, owner: String) : Answer {

        Server.logger.info("Выполнение команды remove_all_by_description")
        var cnt = 0;

        if (postgresDao.removeAllByDescription(description, owner)) {
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
        else return Answer("Ошибка удаления всех элементов по описанию")
    }


    fun removeGreater(name : String, owner: String) : Answer {

        Server.logger.info("Выполнение команды remove_greater")
        var message : String = ""
        try {
            if (postgresDao.removeGreater(name, owner)) {

                if (collectionOfMusicBands.isEmpty()) throw EmptyArgumentException()

                for (i in collectionOfMusicBands.indices) {

                    if (collectionOfMusicBands[i].name == name) {

                        collectionOfMusicBands = collectionOfMusicBands.subList(0, i + 1);
                        message = ("Элемент(ы) удален(ы)")
                        break;
                    }

                    if (i == collectionOfMusicBands.size - 1) message = "Элемент не найден"
                }
            }
            else return Answer("Удаление элементов больше данного невозможно")

            } catch (e : EmptyArgumentException) {
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