package com.batal.elyoum.data

import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupEncryptionUtils {

  private const val ITERATIONS = 12000
  private const val KEY_LENGTH = 256
  private const val TAG_LENGTH_BIT = 128
  private const val IV_LENGTH_BYTE = 12
  private const val SALT_LENGTH_BYTE = 16

  fun encryptBackup(plainJson: String, password: String): String {
    require(password.isNotBlank()) { "كلمة مرور النسخة الاحتياطية مطلوبة" }

    val random = SecureRandom()
    val salt = ByteArray(SALT_LENGTH_BYTE)
    random.nextBytes(salt)

    val iv = ByteArray(IV_LENGTH_BYTE)
    random.nextBytes(iv)

    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
    val secretKey = factory.generateSecret(spec)
    val keySpec = SecretKeySpec(secretKey.encoded, "AES")

    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(TAG_LENGTH_BIT, iv))
    val cipherBytes = cipher.doFinal(plainJson.toByteArray(Charsets.UTF_8))

    val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

    val envelope = JSONObject().apply {
      put("app", "batal_elyoum")
      put("schemaVersion", 4)
      put("format", "AES-256-GCM")
      put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
      put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
      put("ciphertext", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
      put("createdAt", dateStr)
    }

    return envelope.toString(2)
  }

  fun decryptBackup(encryptedEnvelopeJson: String, password: String): String {
    require(password.isNotBlank()) { "كلمة مرور النسخة الاحتياطية مطلوبة" }

    val envelope = try {
      JSONObject(encryptedEnvelopeJson)
    } catch (e: Exception) {
      throw IllegalArgumentException("تنسيق ملف النسخة الاحتياطية غير صالح", e)
    }

    val app = envelope.optString("app")
    if (app != "batal_elyoum") {
      throw IllegalArgumentException("الملف لا يخص تطبيق بطل اليوم")
    }

    val saltBase64 = envelope.getString("salt")
    val ivBase64 = envelope.getString("iv")
    val cipherBase64 = envelope.getString("ciphertext")

    val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
    val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
    val cipherBytes = Base64.decode(cipherBase64, Base64.NO_WRAP)

    return try {
      val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
      val secretKey = factory.generateSecret(spec)
      val keySpec = SecretKeySpec(secretKey.encoded, "AES")

      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(TAG_LENGTH_BIT, iv))
      val decryptedBytes = cipher.doFinal(cipherBytes)
      String(decryptedBytes, Charsets.UTF_8)
    } catch (e: Exception) {
      throw SecurityException("كلمة المرور غير صحيحة أو ملف النسخة الاحتياطية تالف", e)
    }
  }
}
