package server.Managers

import client.Managers.FileManager
import general.commands.*
import general.AppIO.CommandSerialize
import main.resources.commands.*
import general.AppIO.Answer
import server.serverWork.Server
import java.util.concurrent.locks.ReentrantReadWriteLock


/**
 * Класс для работы с командами, введенными пользователем
 * Хранит все команды и историю последних 6 введенных команд
 */
class CommandManager(
    private val helpCommand: HelpCommand,
    private val addCommand: AddCommand,
    private val clearCommand: ClearCommand,
    private val countCommand: CountCommand,
    private val executeScriptCommand: ExecuteScriptCommand,
    private val historyCommand: HistoryCommand,
    private val infoCommand: InfoCommand,
    private val printFieldCommand: PrintFieldCommand,
    private val removeAllCommand: RemoveAllCommand,
    private val removeByID: RemoveByID,
    private val removeFirstCommand: RemoveFirstCommand,
    private val removeGreaterCommand: RemoveGreaterCommand,
    private val showCommand: ShowCommand,
    private val updateIDCommand: UpdateIDCommand,
    private val exitCommand : ExitCommand,
    private val collectionManager: CollectionManager,
    private val fileManager: FileManager
) {

    private val MAXLENGTH = 6;
    private var commands : MutableList<Command> = mutableListOf();
    private var commandsHistory : MutableList<String> = mutableListOf();
    private val readWriteLock = ReentrantReadWriteLock()


    init {
        commands.add(helpCommand)
        commands.add(addCommand)
        commands.add(clearCommand)
        commands.add(countCommand)
        commands.add(executeScriptCommand)
        commands.add(historyCommand)
        commands.add(infoCommand)
        commands.add(printFieldCommand)
        commands.add(removeAllCommand)
        commands.add(removeByID)
        commands.add(removeFirstCommand)
        commands.add(removeGreaterCommand)
        commands.add(showCommand)
        commands.add(updateIDCommand)
        commands.add(exitCommand)
    }


    fun addToHistory(command : String){

        if(commandsHistory.size == 6) {

            commandsHistory = commandsHistory.subList(1, MAXLENGTH)
            commandsHistory.add(command);

        }
        else commandsHistory.add(command);

    }

    private fun getCommandsString() : MutableList<String>{

        val commandsString = mutableListOf<String>();
        for(i in commands.indices){

            commandsString.add(commands[i].getNameCommand().split(' ')[0])

        }

        return commandsString;

    }

    private fun help() : Answer {

        val result = StringBuilder()
        for(i in commands.indices){

            result.append(commands[i].toString() + "\n")

        }
        return Answer(result.toString())
    }


    private fun executeScript(fileName : String) : Answer {

        val list =  fileManager.scripReader(fileName);
        var i = 0;
        while(i < list.size){

            val localList = list[i].split(' ')
            if(localList[0] in getCommandsString()){


                when{

                    localList[0] == "add" && localList.size == 1-> {
                        collectionManager.add(null, list.subList(i + 1, i + 12));
                        i += 12;
                    };

                    localList[0] == "update" && localList.size == 2 -> {

                        collectionManager.update(localList[1], null, list.subList(i + 1, i + 12))
                        i += 12;

                    }

                    else ->{

                        if(localList.size == 1) launchCommand(CommandSerialize(localList[0]))
                        else launchCommand(CommandSerialize(localList[0], localList[1]))
                        i += 1;

                    }
                }
            }
        }
        return Answer("Команда executeScript выполнена")
    }

    @Synchronized
    fun launchCommand(userCommand: CommandSerialize) : Answer {

        when{

            userCommand.getNameCommand() == "help"-> return help()

            userCommand.getNameCommand() == "info" ->  return collectionManager.info()

            userCommand.getNameCommand() == "show" ->  return collectionManager.show()

            userCommand.getNameCommand() == "add" -> return collectionManager.add(userCommand.getMusicBand())

            userCommand.getNameCommand() == "update" -> return collectionManager.update(userCommand.getCommandArgument()!!, userCommand.getMusicBand())

            userCommand.getNameCommand() == "remove_by_id" -> return collectionManager.remove(userCommand.getCommandArgument()!!)

            userCommand.getNameCommand() == "clear" -> return collectionManager.clear()

//            userCommand.getNameCommand() == "execute_script" -> return executeScript(userCommand.getCommandArgument()!!);


            userCommand.getNameCommand() == "exit" -> {

                Server.logger.info("Клиент завершил работу")
                save();
                return Answer("Программа завершена")
            }

            userCommand.getNameCommand() == "remove_first" -> return collectionManager.removeFirst()

            userCommand.getNameCommand() == "remove_greater" -> return collectionManager.removeGreater(userCommand.getCommandArgument()!!);

            userCommand.getNameCommand() == "history" -> return history();

            userCommand.getNameCommand() == "remove_all_by_description" -> return collectionManager.removeAllByDescription(userCommand.getCommandArgument()!!);

            userCommand.getNameCommand() == "count_less_than_number_of_participants" -> return collectionManager.countLessThan(userCommand.getCommandArgument()!!);

            userCommand.getNameCommand() == "print_field_descending_front_man" -> return collectionManager.printlnFrontManDescending();

            else -> {
                Server.logger.info("CommandManager : Команда не обнаружена");
                return Answer("Команда не найдена")
            }


        }



    }

    private fun save(){

        collectionManager.save()

    }

    private fun history() : Answer {

        val result = StringBuilder()
        commandsHistory.forEach{
            result.append(it + "\n");
        }
        return Answer(result.toString())

    }

}