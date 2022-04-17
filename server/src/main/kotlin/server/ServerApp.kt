package server

import client.Client
import client.Modules.ModuleOfCommandFinder
import general.AppIO.InputData
import general.commands.*
import main.resources.commands.*
import server.Modules.ModuleOfCollectionManager
import server.Modules.ModuleOfCommandManager
import server.Modules.ModuleOfFileManager


fun main(){

    /**
     * @author Dmitry Kaplan P3112
     * Лабораторная работа №7 по программированию
     * Главная функция, которая вызывает Server
     */

    val outputData = "outputData";

//    val databaseConnection = DatabaseConnection();

    val moduleOfFileManager = ModuleOfFileManager(outputData)
    val moduleOfCollectionManager = ModuleOfCollectionManager(moduleOfFileManager)
    val moduleOfCommandManager = ModuleOfCommandManager(
        moduleOfCollectionManager,
        moduleOfFileManager ,
        HelpCommand(),
        AddCommand(),
        ClearCommand(),
        CountCommand(),
        ExecuteScriptCommand(),
        HistoryCommand(),
        InfoCommand(),
        PrintFieldCommand(),
        RemoveAllCommand(),
        RemoveByID(),
        RemoveFirstCommand(),
        RemoveGreaterCommand(),
        ShowCommand(),
        UpdateIDCommand(),
        ExitCommand()
    )
    val commandManager = moduleOfCommandManager.commandManager()
    val server = Server(commandManager, 4004, 60*10000)
    val inputData = InputData()
    val moduleOfCommandFinder = ModuleOfCommandFinder(inputData)
    val commandFinder = moduleOfCommandFinder.commandFinder()
    val client = Client(commandFinder, 4004, "localhost")
    server.run()
//        Thread(server).start();
//        Thread(client).start();




}



