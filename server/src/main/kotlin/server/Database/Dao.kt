package server.Database

import JavaClasses.MusicBand

abstract class Dao{

    abstract fun update(id: String, musicBand: MusicBand?)
    abstract fun add(command : MusicBand?)
    abstract fun show()
    abstract fun clear()
    abstract fun remove(id : String)
    abstract fun removeFirst()
    abstract fun removeAllByDescription(description : String)
    abstract fun removeGreater(name : String)
    abstract fun countLessThan(numberOfParticipants : String)
    abstract fun printlnFrontManDescending()


}