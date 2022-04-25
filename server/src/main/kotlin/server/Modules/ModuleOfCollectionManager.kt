package server.Modules

import client.Managers.FileManager
import dev.shustoff.dikt.Create
import dev.shustoff.dikt.CreateSingle
import dev.shustoff.dikt.ProvideSingle
import dev.shustoff.dikt.UseModules
import general.AppIO.InputData
import server.Database.PostgresDao
import server.Managers.CollectionManager

@UseModules(ModuleOfFileManager::class, ModuleOfPostgresDao::class)
class ModuleOfCollectionManager(private val moduleOfFileManager: ModuleOfFileManager, private val moduleOfPostgresDao: ModuleOfPostgresDao) {


    @ProvideSingle
    private fun fileManager() : FileManager

    @CreateSingle
    private fun inputData() : InputData

    @Create
    fun collectionManager() : CollectionManager

    @ProvideSingle
    private fun postgresDao() : PostgresDao





}

