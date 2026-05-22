package ci.nsu.mobile.main.data

import android.content.Context
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val context: Context) {
    private lateinit var tokenManager: TokenManager
    private lateinit var apiService: ApiService

    private fun init() {
        tokenManager = TokenManager(context)
        apiService = RetrofitClient.getApiService(tokenManager)
    }

    suspend fun login(login: String, password: String): Result<UserDto> {
        init()
        return try {
            val response = apiService.login(LoginRequest(login, password))
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse != null) {
                    tokenManager.token = authResponse.token
                    val usersResponse = apiService.getUsers()
                    if (usersResponse.isSuccessful && usersResponse.body() != null) {
                        val currentUser = usersResponse.body()?.find { it.login == login }
                        if (currentUser != null) {
                            tokenManager.userId = currentUser.id.toLong()
                        }

                        Result.success(currentUser ?: UserDto(0, login, "", "", null))
                    } else {
                        Result.success(UserDto(0, login, "", "", null))
                    }
                } else {
                    Result.failure(Exception("Token not received"))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<Unit> {
        init()
        return try {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Registration failed: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    suspend fun getUsers(): Result<List<UserDto>> {
        init()
        return try {
            val response = apiService.getUsers()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get users"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    suspend fun getGroups(): Result<List<GroupDto>> {
        init()
        return try {
            val response = apiService.getGroups()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get groups"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP error: ${e.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error: ${e.message}"))
        }
    }

    fun logout() {
        if (::tokenManager.isInitialized) {
            tokenManager.clearToken()
        }
    }
}