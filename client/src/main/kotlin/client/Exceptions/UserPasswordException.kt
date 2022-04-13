package client.Exceptions

class UserPasswordException : Exception() {

    override val message: String? = "Неверный формат пароля"
}