package server.Database

import server.serverWork.Server
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
                val result = BigInteger(1, temp) + BigInteger(1, salt.toByteArray())
                return result.toString()

            } catch (e: NoSuchAlgorithmException) {
                Server.logger.error("Не найден алгоритм хэширования пароля!")
                throw IllegalStateException(e)
            }
        }
    }
}