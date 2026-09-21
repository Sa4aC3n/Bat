package com.batal.elyoum.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
  private const val ITERATIONS = 10_000
  private const val KEY_LENGTH = 256
  private const val SALT_BYTES = 16

  fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(SALT_BYTES)
    random.nextBytes(salt)
    return Base64.encodeToString(salt, Base64.NO_WRAP)
  }

  fun hashPin(pin: String, saltBase64: String): String {
    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    return try {
      val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
      val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      val hash = factory.generateSecret(spec).encoded
      Base64.encodeToString(hash, Base64.NO_WRAP)
    } catch (e: Exception) {
      // Robust fallback to multi-round SHA-256 if PBKDF2 algorithm is unavailable in local test environment
      val md = MessageDigest.getInstance("SHA-256")
      var hash = md.digest(salt + pin.toByteArray(Charsets.UTF_8))
      for (i in 1 until ITERATIONS) {
        hash = md.digest(hash)
      }
      Base64.encodeToString(hash, Base64.NO_WRAP)
    }
  }

  fun verifyPin(enteredPin: String, saltBase64: String, expectedHash: String): Boolean {
    if (enteredPin.length != 6) return false
    val calculated = hashPin(enteredPin, saltBase64)
    return constantTimeEquals(calculated, expectedHash)
  }

  private fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var result = 0
    for (i in a.indices) {
      result = result or (a[i].code xor b[i].code)
    }
    return result == 0
  }

  fun getLockoutDurationMillis(failedAttempts: Int): Long {
    return when {
      failedAttempts < 4 -> 0L
      failedAttempts == 4 -> 30_000L // 30 seconds
      failedAttempts == 5 -> 60_000L // 1 minute
      else -> 300_000L // 5 minutes
    }
  }
}
