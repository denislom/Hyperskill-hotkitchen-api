package hotkitchen.security

import org.mindrot.jbcrypt.BCrypt

fun hashPassword(plain: String): String =
    BCrypt.hashpw(plain, BCrypt.gensalt(12))

fun verifyPassword(plain: String, hash: String): Boolean =
    runCatching { BCrypt.checkpw(plain, hash) }.getOrDefault(false)