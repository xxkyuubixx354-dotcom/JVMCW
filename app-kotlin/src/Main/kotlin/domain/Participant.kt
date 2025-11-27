package domain

import java.util.UUID

data class Participant(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val organization: String = "",
    val dietaryRestrictions: List<String> = emptyList(),
    val accessibilityNeeds: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName"

    fun isValidEmail(): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
