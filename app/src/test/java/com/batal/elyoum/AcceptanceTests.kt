package com.batal.elyoum

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.batal.elyoum.data.AgeGroup
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.data.HeroDao
import com.batal.elyoum.data.HeroDatabase
import com.batal.elyoum.data.HeroRepository
import com.batal.elyoum.data.ParentSecurityEntity
import com.batal.elyoum.data.ParentTaskEntity
import com.batal.elyoum.data.RecurrenceType
import com.batal.elyoum.data.SecurityUtils
import com.batal.elyoum.data.TaskOccurrenceEntity
import com.batal.elyoum.data.TaskOccurrenceStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AcceptanceTests {

  private lateinit var context: Context
  private lateinit var db: HeroDatabase
  private lateinit var dao: HeroDao
  private lateinit var repository: HeroRepository

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, HeroDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.heroDao()
    repository = HeroRepository(context, dao)
  }

  @After
  fun tearDown() {
    db.close()
  }

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
    // 3 failed attempts: not locked
    assertFalse(SecurityUtils.isLockedOut(3, 0L, now))
    assertEquals(0L, SecurityUtils.getRemainingLockoutSeconds(3, 0L, now))

    // 4 failed attempts: 30 seconds lockout
    val lockout30 = now + 30_000L
    assertTrue(SecurityUtils.isLockedOut(4, lockout30, now))
    assertEquals(30L, SecurityUtils.getRemainingLockoutSeconds(4, lockout30, now))
    // 15 seconds later: still locked
    assertTrue(SecurityUtils.isLockedOut(4, lockout30, now + 15_000L))
    assertEquals(15L, SecurityUtils.getRemainingLockoutSeconds(4, lockout30, now + 15_000L))
    // 31 seconds later: unlocked
    assertFalse(SecurityUtils.isLockedOut(4, lockout30, now + 31_000L))

    // 5 failed attempts: 60 seconds lockout
    val lockout60 = now + 60_000L
    assertTrue(SecurityUtils.isLockedOut(5, lockout60, now))
    assertEquals(60L, SecurityUtils.getRemainingLockoutSeconds(5, lockout60, now))

    // 6 or more failed attempts: 300 seconds lockout
    val lockout300 = now + 300_000L
    assertTrue(SecurityUtils.isLockedOut(6, lockout300, now))
    assertEquals(300L, SecurityUtils.getRemainingLockoutSeconds(6, lockout300, now))
  }

  // ==========================================
  // 2. Acceptance Test: Rejection of Parent Operations When Session is Locked
  // ==========================================

  @Test
  fun `test parent operations are rejected when session is locked`() = runBlocking {
    repository.lockParentSession()
    assertFalse(repository.isParentSessionActive())

    // 1. Rejection of child creation
    try {
      repository.createChildProfile("أحمد", AgeGroup.AGE_7_9, "avatar_star")
      fail("Expected SecurityException when creating child while session is locked")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة"))
    }

    // 2. Rejection of task creation
    try {
      repository.createParentTask(
        title = "ترتيب الغرفة",
        description = "ترتيب السرير",
        requiresApproval = true,
        recurrenceType = RecurrenceType.DAILY,
        targetDaysOfWeek = emptyList(),
        assignedChildIds = emptyList()
      )
      fail("Expected SecurityException when creating task while session is locked")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة"))
    }

    // 3. Rejection of approval and retry
    try {
      repository.approveTaskOccurrence("occ_test_id")
      fail("Expected SecurityException when approving task while session is locked")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة"))
    }

    try {
      repository.retryTaskOccurrence("occ_test_id", "ملاحظة لطيفة")
      fail("Expected SecurityException when retrying task while session is locked")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة"))
    }
  }

  @Test
  fun `test parent operations succeed after authenticating session`() = runBlocking {
    repository.lockParentSession()
    assertFalse(repository.isParentSessionActive())

    // Setup initial PIN unlocks session
    val setupOk = repository.setupInitialPin("654321")
    assertTrue(setupOk)
    assertTrue(repository.isParentSessionActive())

    // Parent operation succeeds
    val childId = repository.createChildProfile("أحمد", AgeGroup.AGE_7_9, "avatar_star")
    assertNotNull(childId)

    val taskId = repository.createParentTask(
      title = "سقي النباتات",
      description = "سقي الورد الصغير",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId)
    )
    assertNotNull(taskId)
  }

  @Test
  fun `test setup initial pin cannot overwrite existing pin and reset requires device auth`() = runBlocking {
    val firstSetup = repository.setupInitialPin("112233")
    assertTrue(firstSetup)

    // Attempting to call setupInitialPin again when PIN is already configured must throw IllegalStateException
    try {
      repository.setupInitialPin("445566")
      fail("Expected IllegalStateException when trying to overwrite existing configured PIN")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("مسبقاً"))
    }

    // Resetting PIN without device authentication must fail with SecurityException
    try {
      repository.resetPinWithDeviceAuth("445566", isDeviceAuthConfirmed = false)
      fail("Expected SecurityException when resetting PIN without confirmed device authentication")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("أمان الجهاز"))
    }

    // Resetting PIN with confirmed device authentication succeeds
    val resetOk = repository.resetPinWithDeviceAuth("445566", isDeviceAuthConfirmed = true)
    assertTrue(resetOk)

    // Verify new PIN works
    val verifyOld = repository.verifyPin("112233")
    assertTrue(verifyOld is HeroRepository.PinCheckResult.IncorrectPin)

    val verifyNew = repository.verifyPin("445566")
    assertTrue(verifyNew is HeroRepository.PinCheckResult.Success)
  }

  // ==========================================
  // 3. Acceptance Test: Child Data Isolation
  // ==========================================

  @Test
  fun `test child profile and task occurrence data isolation`() = runBlocking {
    repository.setParentSessionAuthenticatedForTesting(true)

    val child1Id = repository.createChildProfile("بطل القراءة", AgeGroup.AGE_7_9, "avatar_star")
    val child2Id = repository.createChildProfile("فارس العطاء", AgeGroup.AGE_4_6, "avatar_shield")

    assertNotEquals(child1Id, child2Id)

    // Create task specifically for Child 1
    val task1Id = repository.createParentTask(
      title = "قراءة قصة مفيدة",
      description = "قراءة صفحتين بهدوء",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )

    // Create task specifically for Child 2
    val task2Id = repository.createParentTask(
      title = "مساعدة الأخوة",
      description = "مشاركة الألعاب بلطف",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child2Id),
      startDate = "2026-09-21"
    )

    // Sync occurrences for both children
    repository.syncOccurrencesForChildAndDate(child1Id, "2026-09-21")
    repository.syncOccurrencesForChildAndDate(child2Id, "2026-09-21")

    val occurrencesChild1 = repository.getOccurrencesForChild(child1Id, "2026-09-21").first()
    val occurrencesChild2 = repository.getOccurrencesForChild(child2Id, "2026-09-21").first()

    // Child 1 has ONLY Task 1
    assertEquals(1, occurrencesChild1.size)
    assertEquals(task1Id, occurrencesChild1[0].taskId)
    assertEquals(child1Id, occurrencesChild1[0].childId)

    // Child 2 has ONLY Task 2
    assertEquals(1, occurrencesChild2.size)
    assertEquals(task2Id, occurrencesChild2[0].taskId)
    assertEquals(child2Id, occurrencesChild2[0].childId)
  }

  // ==========================================
  // 4. Acceptance Test: Task Occurrence Idempotency
  // ==========================================

  @Test
  fun `test task occurrences are idempotent and prevent duplicates for same day`() = runBlocking {
    repository.setParentSessionAuthenticatedForTesting(true)

    val childId = repository.createChildProfile("بطل النظام", AgeGroup.AGE_7_9, "avatar_star")
    val taskId = repository.createParentTask(
      title = "ترتيب السرير",
      description = "ترتيب الغطاء والوسادة",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId),
      startDate = "2026-09-21"
    )

    // Sync multiple times for the same date
    repository.syncOccurrencesForChildAndDate(childId, "2026-09-21")
    repository.syncOccurrencesForChildAndDate(childId, "2026-09-21")
    repository.syncOccurrencesForChildAndDate(childId, "2026-09-21")

    val occurrences = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(1, occurrences.size)
    assertEquals(taskId, occurrences[0].taskId)
  }

  // ==========================================
  // 5. Acceptance Test: Task State Machine & Approval Enforcement
  // ==========================================

  @Test
  fun `test occurrence state transitions and approval enforcement`() = runBlocking {
    repository.setParentSessionAuthenticatedForTesting(true)

    val childId = repository.createChildProfile("مريم", AgeGroup.AGE_7_9, "avatar_flower")
    val taskId = repository.createParentTask(
      title = "المساعدة في إعداد المائدة",
      description = "وضع الأطباق بلطف",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId),
      startDate = "2026-09-21"
    )

    repository.syncOccurrencesForChildAndDate(childId, "2026-09-21")
    val occ = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]

    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, occ.status)
    assertTrue(occ.requiresApprovalSnapshot)

    // 1. Child submits completion for task requiring approval -> must transition to PENDING_APPROVAL, NOT COMPLETED
    repository.submitChildTaskCompletion(occ.id, requiresApprovalSnapshot = true)
    val submittedOcc = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]
    assertEquals(TaskOccurrenceStatus.PENDING_APPROVAL.code, submittedOcc.status)

    // 2. Parent reviews and requests gentle retry with encouraging note
    val feedback = "محاولة ممتازة! هيا نجرب معًا وضع الملاعق على اليمين."
    repository.retryTaskOccurrence(occ.id, gentleNote = feedback)
    val retriedOcc = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]
    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, retriedOcc.status)
    assertEquals(feedback, retriedOcc.parentFeedbackNote)

    // 3. Child re-submits task
    repository.submitChildTaskCompletion(occ.id, requiresApprovalSnapshot = true)
    val resubmittedOcc = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]
    assertEquals(TaskOccurrenceStatus.PENDING_APPROVAL.code, resubmittedOcc.status)

    // 4. Parent approves task -> transitions to COMPLETED
    repository.approveTaskOccurrence(occ.id)
    val approvedOcc = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, approvedOcc.status)
  }

  @Test
  fun `test gentle skip does not penalize child`() = runBlocking {
    repository.setParentSessionAuthenticatedForTesting(true)

    val childId = repository.createChildProfile("سارة", AgeGroup.AGE_4_6, "avatar_star")
    val taskId = repository.createParentTask(
      title = "سقي النباتات",
      description = "سقي الوردة الصغيرة",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId),
      startDate = "2026-09-21"
    )

    repository.syncOccurrencesForChildAndDate(childId, "2026-09-21")
    val occ = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]

    // Child skips task for today
    repository.skipTaskToday(occ.id)
    val skippedOcc = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]
    assertEquals(TaskOccurrenceStatus.SKIPPED.code, skippedOcc.status)
  }

  // ==========================================
  // 6. Acceptance Test: Database Migration from v1 to v2 to v3 with Data Retention
  // ==========================================

  @Test
  fun `test database migration preserves existing data across schema versions`() {
    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name("migration_test_db")
      .callback(object : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: SupportSQLiteDatabase) {
          // Schema Version 1 (Pre-Parent-Section tables)
          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `honored_heroes` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `name` TEXT NOT NULL,
              `title` TEXT NOT NULL,
              `category` TEXT NOT NULL,
              `honoredDate` TEXT NOT NULL,
              `tasksCompletedCount` INTEGER NOT NULL,
              `createdAt` INTEGER NOT NULL
            )
          """.trimIndent())
          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `favorite_heroes` (
              `heroId` TEXT NOT NULL PRIMARY KEY,
              `addedAt` INTEGER NOT NULL
            )
          """.trimIndent())
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
      })
      .build()

    val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
    val dbSqlite = openHelper.writableDatabase

    // Insert pre-existing v1 data
    dbSqlite.execSQL("""
      INSERT INTO `honored_heroes` VALUES ('ibn_battuta', 'ابن بطوطة', 'أمير الرحالة', 'DISCOVERY', '2026-09-20', 3, 1726800000000)
    """.trimIndent())

    // Execute Migration 1 -> 2
    HeroDatabase.MIGRATION_1_2.migrate(dbSqlite)

    // Insert v2 security row
    dbSqlite.execSQL("""
      INSERT INTO `parent_security` (`id`, `pinSalt`, `pinHash`, `failedAttempts`, `lockoutUntilMillis`, `isConfigured`, `updatedAtMillis`)
      VALUES (1, 'salt123', 'hash456', 0, 0, 1, 1726800000000)
    """.trimIndent())

    // Execute Migration 2 -> 3
    HeroDatabase.MIGRATION_2_3.migrate(dbSqlite)

    // Verify v1 data is still intact
    val cursorHero = dbSqlite.query("SELECT id, name, title FROM honored_heroes WHERE id = 'ibn_battuta'")
    assertTrue("v1 record must exist after migration", cursorHero.moveToFirst())
    assertEquals("ابن بطوطة", cursorHero.getString(1))
    cursorHero.close()

    // Verify v3 parent_security columns exist and contain defaults
    val cursorSec = dbSqlite.query("SELECT id, pinSalt, pinHash, algoVersion, iterations, algorithm FROM parent_security WHERE id = 1")
    assertTrue("parent_security record must exist after migration", cursorSec.moveToFirst())
    assertEquals("salt123", cursorSec.getString(1))
    assertEquals("hash456", cursorSec.getString(2))
    assertEquals(1, cursorSec.getInt(3)) // algoVersion default 1
    assertEquals(10000, cursorSec.getInt(4)) // iterations default 10000
    assertEquals("PBKDF2WithHmacSHA256", cursorSec.getString(5)) // algorithm default
    cursorSec.close()

    dbSqlite.close()
  }

  // ==========================================
  // 7. Acceptance Test: Educational Praise and Tone
  // ==========================================

  @Test
  fun `test random effort praise is positive and free of negative gamification`() {
    for (i in 1..20) {
      val msg = repository.getRandomEffortPraise()
      assertTrue(msg.isNotBlank())
      assertFalse(msg.contains("خصم"))
      assertFalse(msg.contains("خسرت"))
      assertFalse(msg.contains("عقوبة"))
      assertFalse(msg.contains("إنذار"))
    }
  }
}
