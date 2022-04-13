package client.Exceptions

class UserLoginException : Exception() {

    override val message: String? = "Неверный формат логина"
}