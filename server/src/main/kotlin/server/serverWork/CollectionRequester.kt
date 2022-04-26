package server.serverWork

import general.AppIO.Answer
import general.AppIO.CommandSerialize
import server.Managers.CommandManager
import java.util.concurrent.Callable

class CollectionRequester : Callable<Answer> {
    private val commandManager : CommandManager
    private val command : CommandSerialize
    private val nameOfPerson : String;

    constructor(commandManager: CommandManager, command : CommandSerialize, nameOfPerson : String){
        this.commandManager = commandManager
        this.command = command
        this.nameOfPerson = nameOfPerson
    }

    override fun call(): Answer {
        return commandManager.launchCommand(command, nameOfPerson)
    }
}