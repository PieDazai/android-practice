package ci.nsu.mobile.main.deposit


import android.content.Context
import ci.nsu.mobile.main.data.AuthRepository
import ci.nsu.mobile.main.data.TokenManager

class ServiceLocator(context: Context) {

    val database by lazy {
        AppDatabase.getDatabase(context)
    }

    val authRepository by lazy {
        AuthRepository(context)
    }

    val depositRepository by lazy {
        DepositRepository(database.depositDao())
    }

    val tokenManager by lazy {
        TokenManager(context)
    }
}