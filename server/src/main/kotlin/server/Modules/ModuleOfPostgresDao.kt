package server.Modules

import dev.shustoff.dikt.Create
import server.Database.PostgresDao
import server.Managers.DatabaseConnection

class ModuleOfPostgresDao(private val databaseConnection: DatabaseConnection) {

    @Create
    fun postgresDao() : PostgresDao
}