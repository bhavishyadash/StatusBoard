package com.example.statusboard.domain.model

data class AuthSelection(
    val usePhone: Boolean = false,
    val useEmail: Boolean = false,
    val useUsernameLogin: Boolean = false,
    val useGoogle: Boolean = false,

    val phone: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val nickname: String = ""
)
fun AuthSelection.validate(): String? {
    // 0. Nickname required always
    if (nickname.isBlank()) {
        return "Nickname is required."
    }

    // 1. Google only case
    if (useGoogle && !usePhone && !useEmail && !useUsernameLogin) {
        // No password / username / email required here
        return null
    }

    // 2. If ANY manual method chosen (phone OR email OR username)
    val anyManual = usePhone || useEmail || useUsernameLogin
    if (anyManual) {
        if (password.isBlank()) {
            return "Password is required for this login method."
        }
    }

    // 3. If phone + email BOTH chosen → username also mandatory
    if (usePhone && useEmail) {
        if (username.isBlank()) {
            return "Username is required when using both phone and email."
        }
    }

    // 4. Field-level checks (basic sanity)
    if (usePhone && phone.isBlank()) {
        return "Phone number is required."
    }
    if (useEmail && email.isBlank()) {
        return "Email is required."
    }
    if (useUsernameLogin && username.isBlank()) {
        return "Username is required."
    }

    // All good 🎉
    return null
}