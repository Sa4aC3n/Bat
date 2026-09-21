package com.batal.elyoum.data

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
  const val DEFAULT_ALGORITHM = "PBKDF2WithHmacSHA256"
  const val DEFAULT_ITERATIONS = 10_000
  const val DEFAULT_KEY_LENGTH = 256
  const val DEFAULT_ALGO_VERSION = 1
  const val LEGACY_ALGORITHM_SHA256_MULTI = "SHA256_MULTI_ROUND"

  private const val SALT_BYTES = 16

  fun generateSalt(): String {
    val random = SecureRandom()
    val salt = ByteArray(SALT_BYTES)
    random.nextBytes(salt)
    return Base64.encodeToString(salt, Base64.NO_WRAP)
  }

  fun isValidPinFormat(pin: String): Boolean {
    return pin.length == 6 && pin.all { it.isDigit() }
  }

  fun hashPin(
    pin: String,
    saltBase64: String,
    algorithm: String = DEFAULT_ALGORITHM,
    iterations: Int = DEFAULT_ITERATIONS,
    keyLength: Int = DEFAULT_KEY_LENGTH
  ): String {
    require(isValidPinFormat(pin)) { "يجب أن يتكون رمز المرور من ٦ أرقام" }
    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    return when (algorithm) {
      DEFAULT_ALGORITHM -> {
        val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance(algorithm)
        val hash = factory.generateSecret(spec).encoded
        Base64.encodeToString(hash, Base64.NO_WRAP)
      }
      LEGACY_ALGORITHM_SHA256_MULTI -> {
        val md = MessageDigest.getInstance("SHA-256")
        var hash = md.digest(salt + pin.toByteArray(Charsets.UTF_8))
        for (i in 1 until iterations) {
          hash = md.digest(hash)
        }
        Base64.encodeToString(hash, Base64.NO_WRAP)
      }
      else -> throw IllegalArgumentException("خوارزمية التشفير غير مدعومة: $algorithm")
    }
  }

  fun verifyPin(
    enteredPin: String,
    saltBase64: String,
    expectedHash: String,
    algorithm: String = DEFAULT_ALGORITHM,
    iterations: Int = DEFAULT_ITERATIONS,
    keyLength: Int = DEFAULT_KEY_LENGTH
  ): Boolean {
    if (!isValidPinFormat(enteredPin)) return false
    val calculated = try {
      hashPin(enteredPin, saltBase64, algorithm, iterations, keyLength)
    } catch (e: Exception) {
      return false
    }
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

  fun isLockedOut(failedAttempts: Int, lockoutUntilMillis: Long, currentMillis: Long = System.currentTimeMillis()): Boolean {
    return lockoutUntilMillis > currentMillis
  }

  fun getRemainingLockoutSeconds(failedAttempts: Int, lockoutUntilMillis: Long, currentMillis: Long = System.currentTimeMillis()): Long {
    val diff = lockoutUntilMillis - currentMillis
    return if (diff > 0) (diff + 999) / 1000 else 0L
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
