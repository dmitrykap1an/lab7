package server.Modules

import client.Managers.FileManager
import dev.shustoff.dikt.Create
import dev.shustoff.dikt.ProvideSingle
import dev.shustoff.dikt.UseModules
import general.commands.*
import main.resources.commands.*
import server.Managers.CollectionManager
import server.Managers.CommandManager


@UseModules(ModuleOfCollectionManager::class, ModuleOfFileManager::class)
class ModuleOfCommandManager(
    private val moduleOfCollectionManager: ModuleOfCollectionManager,
    private val moduleOfFileManager: ModuleOfFileManager,
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
) {

    @Create
    fun commandManager() : CommandManager

    @ProvideSingle
    private fun collectionManager() : CollectionManager;

    @ProvideSingle
    private fun fileManager() : FileManager;


}