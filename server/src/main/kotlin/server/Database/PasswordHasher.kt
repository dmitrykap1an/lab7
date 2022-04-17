package server.Database

import server.Server
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class PasswordHasher {

    companion object{

        fun hashPassword(password: String, salt : String) : String {
            try {

                val messageDigest = MessageDigest.getInstance("SHA-256")
                val bytes = messageDigest.digest(password.toByteArray(Charsets.UTF_8))
                val temp = messageDigest.digest(bytes)
                return BigInteger(1, temp).toString()
            } catch (e: NoSuchAlgorithmException) {
                Server.logger.error("Не найден алгоритм хэширования пароля!")
                throw IllegalStateException(e)
            }
        }
    }
}