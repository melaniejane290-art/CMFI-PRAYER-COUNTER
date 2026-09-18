package com.example.model

/**
 * Representation of an authenticated user account in the prayer proclamation app.
 */
data class UserAccount(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val providerId: String = "firebase",
    val role: String = "devotee",
    val creationTimestamp: Long = 0L,
    val lastSignInTimestamp: Long = 0L
) {
    val isAdmin: Boolean
        get() = role.equals("admin", ignoreCase = true) ||
                email?.lowercase()?.contains("admin") == true ||
                email.equals("melaniejane290@gmail.com", ignoreCase = true)

    val displayTitle: String
        get() = displayName?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")
            ?: "Prayer Devotee"

    val initials: String
        get() {
            val name = displayName?.trim()
            if (!name.isNullOrEmpty()) {
                val parts = name.split(" ").filter { it.isNotBlank() }
                return if (parts.size >= 2) {
                    "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
                } else {
                    name.take(2).uppercase()
                }
            }
            val mail = email?.trim()
            if (!mail.isNullOrEmpty()) {
                return mail.take(2).uppercase()
            }
            return "✝️"
        }
}

/**
 * Authentication dialog view modes.
 */
enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

/**
 * UI State for authentication workflows.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val currentAuthMode: AuthMode = AuthMode.SIGN_IN
)
