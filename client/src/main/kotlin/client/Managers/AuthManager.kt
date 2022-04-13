package client.Managers

class AuthManager {

    companion object {
        fun handle(): UserSerialize {
            return when(Asker.askQuestion("Вы уже зарегестрированы в системе?")){

                true -> UserSerialize(TypeOfAuth.Registered, Asker.askLogin(), Asker.askPassword())

                else -> UserSerialize(TypeOfAuth.NotRegistered, Asker.askLogin(), Asker.askPassword())
            }

        }
    }
}