package server.Modules

import dev.shustoff.dikt.Create
import dev.shustoff.dikt.CreateSingle
import dev.shustoff.dikt.ProvideSingle
import dev.shustoff.dikt.UseModules
import general.AppIO.InputData
import client.Managers.FileManager
import server.Database.PostgresDao
import server.Managers.CollectionManager
import server.Managers.DatabaseConnection

@UseModules(ModuleOfFileManager::class)
class ModuleOfCollectionManager(private val moduleOfFileManager: ModuleOfFileManager) {


    @ProvideSingle
    private fun fileManager() : FileManager

    @CreateSingle
    private fun inputData() : InputData

    @Create
    fun collectionManager() : CollectionManager





}

