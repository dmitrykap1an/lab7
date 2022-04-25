package general.AppIO

import java.io.Serializable

data class MessageSerialize(var messages : MutableList<String>) : Serializable{

    init{
        messages = mutableListOf()
    }

}