package server

import general.commands.*
import main.resources.commands.*
import server.Managers.DatabaseConnection
import server.Modules.ModuleOfCollectionManager
import server.Modules.ModuleOfCommandManager
import server.Modules.ModuleOfFileManager
import server.Modules.ModuleOfPostgresDao
import server.serverWork.Server
import java.io.FileReader
import java.util.Properties
import java.util.concurrent.Executors


fun main(){

    /**
     * @author Dmitry Kaplan P3112
     * Лабораторная работа №7 по программированию
     * Главная функция, которая вызывает Server
     */

    val properties = Properties()
    val file = FileReader("/home/newton/IdeaProjects/lab6-master/server/src/main/kotlin/server/databaseInfo.properties")
    properties.load(file)

    val databaseConnection = DatabaseConnection(properties);
    val moduleOfPostgresDao = ModuleOfPostgresDao(databaseConnection)
    val moduleOfCollectionManager = ModuleOfCollectionManager(moduleOfPostgresDao)
    val moduleOfCommandManager = ModuleOfCommandManager(
        moduleOfCollectionManager,
        HelpCommand(),
        AddCommand(),
        ClearCommand(),
        CountCommand(),
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
  val server =  Server(commandManager, 4004)
    server.run()




}



