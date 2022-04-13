package server.Database

import server.Server
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class PasswordHasher {

    companion object{

        fun hashPassword(password: String): String? {
            return try {
                val md = MessageDigest.getInstance("SHA-512")
                val bytes = md.digest(password.toByteArray(StandardCharsets.UTF_8))
                val newPassword = md.digest(bytes).toString()
                newPassword
            } catch (exception: NoSuchAlgorithmException) {
                Server.logger.error("Не найден алгоритм хэширования пароля!")
                throw IllegalStateException(exception)
            }
        }
    }
}