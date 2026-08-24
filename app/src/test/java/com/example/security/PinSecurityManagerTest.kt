package com.example.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinSecurityManagerTest {

    @Test
    fun testPinHashingAndVerification() {
        val pin = "1234"
        val salt = PinSecurityManager.generateSalt()
        val hash = PinSecurityManager.hashPin(pin, salt)

        // Verifying with correct PIN
        assertTrue(PinSecurityManager.verifyPin("1234", hash, salt))

        // Verifying with incorrect PIN
        assertFalse(PinSecurityManager.verifyPin("0000", hash, salt))
        assertFalse(PinSecurityManager.verifyPin("1235", hash, salt))
    }

    @Test
    fun testSaltUniqueness() {
        val salt1 = PinSecurityManager.generateSalt()
        val salt2 = PinSecurityManager.generateSalt()
        assertNotEquals(salt1, salt2)
    }

    @Test
    fun testLockoutCalculation() {
        val now = System.currentTimeMillis()
        val futureLockout = now + 30000L // 30 seconds

        assertTrue(PinSecurityManager.isLockedOut(futureLockout))
        val remaining = PinSecurityManager.getRemainingLockoutSeconds(futureLockout)
        assertTrue(remaining in 28..31)

        val pastLockout = now - 5000L
        assertFalse(PinSecurityManager.isLockedOut(pastLockout))
        assertEquals(0, PinSecurityManager.getRemainingLockoutSeconds(pastLockout))
    }
}
