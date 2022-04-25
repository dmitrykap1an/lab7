package general.AppIO

import java.io.Serializable
data class Answer(private val message : String) : Serializable{


    fun getMessage() = message

}