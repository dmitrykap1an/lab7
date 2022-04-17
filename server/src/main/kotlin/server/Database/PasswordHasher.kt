package server.Database

import server.Server
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class PasswordHasher {

    companion object{

        fun hashPassword(password: String, salt : String) : String {
            try {

                val md = MessageDigest.getInstance("SHA-512")
                val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
                println(password.toByteArray(Charsets.US_ASCII))
                println(password.toByteArray(Charsets.US_ASCII))
                val newPassword = md.digest(bytes).toString() + salt
                println("Password is $newPassword")
                 return newPassword
            } catch (e : NoSuchAlgorithmException) {
                Server.logger.error("Не найден алгоритм хэширования пароля!")
                throw IllegalStateException(e)
            }
        }
    }
}