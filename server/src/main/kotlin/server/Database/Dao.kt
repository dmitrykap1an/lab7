package server.Database

import JavaClasses.MusicBand

abstract class Dao{

    abstract fun update(id: String, musicBand: MusicBand?, owner : String) : Boolean
    abstract fun add(command : MusicBand?, owner: String) : Boolean
    abstract fun clear(owner: String) : Boolean
    abstract fun remove(id : String, owner: String) : Boolean
    abstract fun removeFirst(owner: String) : Boolean
    abstract fun removeAllByDescription(description : String, owner: String) : Boolean
    abstract fun removeGreater(name : String, owner: String) : Boolean
    abstract fun addMusicBandsToCollection() : MutableList<MusicBand>


}