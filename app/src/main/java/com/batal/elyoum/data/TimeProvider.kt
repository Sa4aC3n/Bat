package com.batal.elyoum.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface TimeProvider {
  fun currentTimeMillis(): Long
  fun todayDateString(): String
}

class DefaultTimeProvider : TimeProvider {
  override fun currentTimeMillis(): Long = System.currentTimeMillis()

  override fun todayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date(currentTimeMillis()))
  }
}

/**
 * Controllable TimeProvider for acceptance and unit tests.
 */
class ControllableTimeProvider(
  private var currentMillis: Long = System.currentTimeMillis(),
  private var fixedDateString: String? = null
) : TimeProvider {

  override fun currentTimeMillis(): Long = currentMillis

  override fun todayDateString(): String {
    fixedDateString?.let { return it }
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(Date(currentMillis))
  }

  fun setTimeMillis(millis: Long) {
    currentMillis = millis
  }

  fun setDate(dateStr: String) {
    fixedDateString = dateStr
  }

  fun advanceDays(days: Int) {
    currentMillis += days * 86_400_000L
    fixedDateString = null
  }
}
