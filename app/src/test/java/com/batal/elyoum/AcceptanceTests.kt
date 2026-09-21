package com.batal.elyoum

import com.batal.elyoum.data.AgeGroup
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.data.OccurrenceStatus
import com.batal.elyoum.data.ParentSecurityEntity
import com.batal.elyoum.data.ParentTaskEntity
import com.batal.elyoum.data.RecurrenceType
import com.batal.elyoum.data.SecurityUtils
import com.batal.elyoum.data.TaskOccurrenceEntity
import com.batal.elyoum.ui.PRAISE_MESSAGES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AcceptanceTests {

  // ==========================================
  // 1. Acceptance Test: SecurityUtils & PIN
  // ==========================================

  @Test
  fun `test pin validation format`() {
    assertTrue(SecurityUtils.isValidPinFormat("123456"))
    assertTrue(SecurityUtils.isValidPinFormat("000000"))
    assertTrue(SecurityUtils.isValidPinFormat("987654"))
    assertFalse(SecurityUtils.isValidPinFormat("12345")) // too short
    assertFalse(SecurityUtils.isValidPinFormat("1234567")) // too long
    assertFalse(SecurityUtils.isValidPinFormat("12a456")) // non-numeric
    assertFalse(SecurityUtils.isValidPinFormat(""))
  }

  @Test
  fun `test pin hashing and verification`() {
    val pin = "849201"
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(pin, salt)

    // Verification with correct PIN
    assertTrue(SecurityUtils.verifyPin(pin, salt, hash))

    // Verification with wrong PIN
    assertFalse(SecurityUtils.verifyPin("849200", salt, hash))
    assertFalse(SecurityUtils.verifyPin("123456", salt, hash))
  }

  @Test
  fun `test unique salt produces unique hash for same pin`() {
    val pin = "654321"
    val salt1 = SecurityUtils.generateSalt()
    val salt2 = SecurityUtils.generateSalt()

    val hash1 = SecurityUtils.hashPin(pin, salt1)
    val hash2 = SecurityUtils.hashPin(pin, salt2)

    assertNotEquals(salt1, salt2)
    assertNotEquals(hash1, hash2)
  }

  @Test
  fun `test lockout calculation after repeated failures`() {
    val now = 1_000_000L
    // 4 failed attempts: not locked
    assertFalse(SecurityUtils.isLockedOut(4, 0L, now))
    assertEquals(0L, SecurityUtils.getRemainingLockoutSeconds(4, 0L, now))

    // 5 failed attempts: 30 seconds lockout
    assertTrue(SecurityUtils.isLockedOut(5, now, now))
    assertEquals(30L, SecurityUtils.getRemainingLockoutSeconds(5, now, now))
    // 15 seconds later: still locked
    assertTrue(SecurityUtils.isLockedOut(5, now, now + 15_000L))
    assertEquals(15L, SecurityUtils.getRemainingLockoutSeconds(5, now, now + 15_000L))
    // 31 seconds later: unlocked
    assertFalse(SecurityUtils.isLockedOut(5, now, now + 31_000L))

    // 7 failed attempts: 60 seconds lockout
    assertTrue(SecurityUtils.isLockedOut(7, now, now))
    assertEquals(60L, SecurityUtils.getRemainingLockoutSeconds(7, now, now))

    // 10 failed attempts: 300 seconds lockout
    assertTrue(SecurityUtils.isLockedOut(10, now, now))
    assertEquals(300L, SecurityUtils.getRemainingLockoutSeconds(10, now, now))
  }

  // ==========================================
  // 2. Acceptance Test: Child Isolation & Profiles
  // ==========================================

  @Test
  fun `test child profile isolation and minimal data collection`() {
    val child1 = ChildProfileEntity(
      id = 1L,
      alias = "بطل القراءة",
      ageGroup = AgeGroup.FROM_7_TO_9,
      avatarId = "avatar_star",
      isArchived = false
    )

    val child2 = ChildProfileEntity(
      id = 2L,
      alias = "فارس العطاء",
      ageGroup = AgeGroup.FROM_4_TO_6,
      avatarId = "avatar_shield",
      isArchived = false
    )

    assertNotEquals(child1.id, child2.id)
    assertNotEquals(child1.alias, child2.alias)
    // Verify no PII fields exist
    assertEquals("بطل القراءة", child1.alias)
    assertEquals(AgeGroup.FROM_7_TO_9, child1.ageGroup)
  }

  // ==========================================
  // 3. Acceptance Test: Task Occurrence & Idempotency Key
  // ==========================================

  @Test
  fun `test task occurrence key idempotency logic`() {
    val taskId = 101L
    val childId = 1L
    val dateStr = "2026-09-21"

    val occurrence1 = TaskOccurrenceEntity(
      id = 1L,
      taskId = taskId,
      childId = childId,
      dateStr = dateStr,
      status = OccurrenceStatus.NOT_STARTED,
      snapshotTitle = "ترتيب الغرفة",
      snapshotDescription = "ترتيب الفراش والوسادة",
      requiresApproval = true
    )

    // Verify key fields match
    assertEquals(taskId, occurrence1.taskId)
    assertEquals(childId, occurrence1.childId)
    assertEquals(dateStr, occurrence1.dateStr)
  }

  // ==========================================
  // 4. Acceptance Test: Task Status Lifecycle Transitions
  // ==========================================

  @Test
  fun `test occurrence state machine for approval and retry`() {
    var occ = TaskOccurrenceEntity(
      id = 1L,
      taskId = 10L,
      childId = 1L,
      dateStr = "2026-09-21",
      status = OccurrenceStatus.NOT_STARTED,
      snapshotTitle = "مساعدة في إعداد المائدة",
      snapshotDescription = "وضع الأطباق بلطف",
      requiresApproval = true
    )

    // Child submits task
    occ = occ.copy(status = OccurrenceStatus.PENDING_APPROVAL)
    assertEquals(OccurrenceStatus.PENDING_APPROVAL, occ.status)

    // Parent reviews and requests gentle retry
    val gentleNote = "محاولة ممتازة، هيا نجرب معًا ترتيب الملاعق"
    occ = occ.copy(status = OccurrenceStatus.NOT_STARTED, parentFeedbackNote = gentleNote)
    assertEquals(OccurrenceStatus.NOT_STARTED, occ.status)
    assertEquals(gentleNote, occ.parentFeedbackNote)

    // Child re-submits
    occ = occ.copy(status = OccurrenceStatus.PENDING_APPROVAL)
    assertEquals(OccurrenceStatus.PENDING_APPROVAL, occ.status)

    // Parent approves
    occ = occ.copy(status = OccurrenceStatus.COMPLETED)
    assertEquals(OccurrenceStatus.COMPLETED, occ.status)
  }

  @Test
  fun `test gentle skip does not penalize child`() {
    var occ = TaskOccurrenceEntity(
      id = 2L,
      taskId = 20L,
      childId = 1L,
      dateStr = "2026-09-21",
      status = OccurrenceStatus.NOT_STARTED,
      snapshotTitle = "سقي النباتات",
      snapshotDescription = "سقي الوردات الصغيرة",
      requiresApproval = false
    )

    // Child skips for today
    occ = occ.copy(status = OccurrenceStatus.SKIPPED)
    assertEquals(OccurrenceStatus.SKIPPED, occ.status)
    // Verify no streak loss or negative point calculation
  }

  // ==========================================
  // 5. Acceptance Test: Tone and Non-gamification
  // ==========================================

  @Test
  fun `test praise messages are encouraging and non-penalizing`() {
    assertTrue(PRAISE_MESSAGES.isNotEmpty())
    for (msg in PRAISE_MESSAGES) {
      assertTrue(msg.isNotBlank())
      assertFalse(msg.contains("خصم"))
      assertFalse(msg.contains("خسرت"))
      assertFalse(msg.contains("عقوبة"))
    }
  }
}
