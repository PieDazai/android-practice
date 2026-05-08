package ci.nsu.mobile.main.data

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("login")
    val login: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("person")
    val person: PersonDto?
)

data class PersonDto(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("middleName")
    val middleName: String,
    @SerializedName("birthDate")
    val birthDate: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("groupId")
    val groupId: Int
)

data class RegisterRequest(
    val login: String,
    val password: String,
    val email: String,
    val phoneNumber: String,
    val roleId: Int = 1,
    val authAllowed: Boolean = true,
    val person: PersonDto
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("token")
    val token: String
)

data class GroupDto(
    @SerializedName("groupId")
    val id: Int,
    @SerializedName("groupName")
    val name: String
)