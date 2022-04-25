package client.Managers

import client.Exceptions.AnswerException
import client.Exceptions.UserLoginException
import client.Exceptions.UserPasswordException
import java.util.*

class Asker {

    companion object {
        fun askLogin(): String {

            var login: String
            while (true) {
                try {
                    println("Введите Ваш логин:")
                    login = readLine()!!.trim()

                    if (login == "") throw UserLoginException()
                    break
                } catch (e: NullPointerException) {
                    println("Неверный формат логина")
                } catch (e: UserLoginException) {
                    println("Неверный формат логина")
                }
            }
            return login
        }

        fun askPassword(): String {

            var password: String
            while (true) {
                try {
                    val console = System.console()
                    if(console == null){
                        println("Введите Ваш пароль:")
                        password = readLine()!!.trim()
                    }
                    else{
                        val pass = console.readPassword("Введите Ваш пароль:")
                        password = String(pass)
                    }

                    if (password == "") throw UserPasswordException()
                    break
                } catch (e: NullPointerException) {
                    println("Неверный формат логина")
                } catch (e: UserLoginException) {
                    println("Неверный формат логина")
                }catch (e : UserPasswordException){
                    println("Неверный формат ввода пароля")
                }
            }
            return password

        }

        fun askQuestion(text: String): Boolean {

            println("$text Д/н")

            while (true) {

                try {
                    when (readLine()!!.trim().uppercase(Locale.getDefault())) {
                        "ДА", "Д", "+", "" -> {
                            return true
                            break;
                        }

                        "НЕТ", "Н", "-" -> {

                            return false
                            break;
                        }
                        else -> throw AnswerException()
                    }
                } catch (e: AnswerException) {
                    println("Введите 'д' или 'н'")
                }

            }
        }
    }
}