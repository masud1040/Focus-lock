package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PinSecurityManager {

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt)
    }

    fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray())
        val digest = md.digest(pin.toByteArray())
        return Base64.getEncoder().encodeToString(digest)
    }

    fun verifyPin(pin: String, storedHash: String, storedSalt: String): Boolean {
        if (storedHash.isBlank() || storedSalt.isBlank()) return false
        val computedHash = hashPin(pin, storedSalt)
        // Constant-time comparison to prevent timing attacks
        return MessageDigest.isEqual(computedHash.toByteArray(), storedHash.toByteArray())
    }

    fun isLockedOut(lockoutUntil: Long): Boolean {
        return lockoutUntil > System.currentTimeMillis()
    }

    fun getRemainingLockoutSeconds(lockoutUntil: Long): Int {
        val remaining = lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) (remaining / 1000).toInt() else 0
    }
}
