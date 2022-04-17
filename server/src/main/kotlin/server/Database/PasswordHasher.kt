package server.Database

import server.Server
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class PasswordHasher {

    companion object{

        fun hashPassword(password: String, salt : ByteArray) : String {
            try {

                val md = MessageDigest.getInstance("SHA-512")
                val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8) + salt)
                val newPassword = md.digest(bytes).toString()
                println("Password is $newPassword")
                 return newPassword
            } catch (e : NoSuchAlgorithmException) {
                Server.logger.error("Не найден алгоритм хэширования пароля!")
                throw IllegalStateException(e)
            }
        }
    }
}