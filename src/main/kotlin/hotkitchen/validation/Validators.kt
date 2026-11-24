package hotkitchen.validation

fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(email)
}

fun isValidPassword(password: String): Boolean {
    val valid: Boolean = (password.length >= 6 && password.any { it ->
        it.isLetter() && password.any { it.isDigit() }})
    return valid
}