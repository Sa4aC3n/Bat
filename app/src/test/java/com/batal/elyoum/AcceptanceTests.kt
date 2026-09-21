package com.batal.elyoum

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.batal.elyoum.data.AgeGroup
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.data.ControllableTimeProvider
import com.batal.elyoum.data.DailyDeedProgressEntity
import com.batal.elyoum.data.FavoriteHeroEntity
import com.batal.elyoum.data.HeroDao
import com.batal.elyoum.data.HeroDatabase
import com.batal.elyoum.data.HeroRepository
import com.batal.elyoum.data.HonoredHeroEntity
import com.batal.elyoum.data.ParentSecurityEntity
import com.batal.elyoum.data.ParentTaskEntity
import com.batal.elyoum.data.RecurrenceType
import com.batal.elyoum.data.SecurityUtils
import com.batal.elyoum.data.TaskOccurrenceEntity
import com.batal.elyoum.data.TaskOccurrenceStatus
import com.batal.elyoum.ui.HeroViewModel
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
  private lateinit var timeProvider: ControllableTimeProvider

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, HeroDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    dao = db.heroDao()
    timeProvider = ControllableTimeProvider(currentMillis = 1726800000000L, fixedDateString = "2026-09-21")
    repository = HeroRepository(context, dao, timeProvider)
  }

  @After
  fun tearDown() {
    db.close()
  }

  // ==========================================
  // 1. Acceptance Test: Real ActivityScenario Launch (Requirement 1)
  // ==========================================

  @Test
  fun `test MainActivity launches successfully via real ActivityScenario`() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.moveToState(Lifecycle.State.RESUMED)
    scenario.onActivity { activity ->
      assertNotNull(activity)
      assertFalse(activity.isFinishing)
    }
    scenario.close()
  }

  // ==========================================
  // 2. Acceptance Test: SecurityUtils & PIN Format
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
  fun `test pbkdf2 hashing generates unique salt and verifies correctly`() {
    val pin = "654321"
    val salt1 = SecurityUtils.generateSalt()
    val salt2 = SecurityUtils.generateSalt()

    assertNotEquals("Generated salts must be unique", salt1, salt2)

    val hash1 = SecurityUtils.hashPin(pin, salt1)
    val hash2 = SecurityUtils.hashPin(pin, salt2)
    assertNotEquals("Hashes for different salts must be distinct", hash1, hash2)

    assertTrue("Correct PIN must verify against hash1", SecurityUtils.verifyPin(pin, salt1, hash1))
    assertTrue("Correct PIN must verify against hash2", SecurityUtils.verifyPin(pin, salt2, hash2))
    assertFalse("Wrong PIN must fail verification", SecurityUtils.verifyPin("111111", salt1, hash1))
  }

  // ==========================================
  // 3. Acceptance Test: PIN Rate Limiting & Lockout
  // ==========================================

  @Test
  fun `test rate limiting and incremental lockout periods`() = runBlocking {
    repository.setupInitialPin("123456")

    // Attempt 1, 2, 3: Incorrect PIN, no lockout
    for (attempt in 1..3) {
      val res = repository.verifyPin("999999")
      assertTrue(res is HeroRepository.PinCheckResult.IncorrectPin)
      val incorrect = res as HeroRepository.PinCheckResult.IncorrectPin
      assertEquals(attempt, incorrect.failedAttempts)
      assertEquals(0L, incorrect.lockoutSecondsRemaining)
    }

    // Attempt 4: 30 seconds lockout
    val res4 = repository.verifyPin("999999")
    assertTrue(res4 is HeroRepository.PinCheckResult.IncorrectPin)
    val inc4 = res4 as HeroRepository.PinCheckResult.IncorrectPin
    assertEquals(4, inc4.failedAttempts)
    assertTrue("Lockout must be between 25 and 30 seconds", inc4.lockoutSecondsRemaining in 25..30)

    // While locked out, entering any PIN returns LockedOut
    val lockedCheck = repository.verifyPin("123456")
    assertTrue(lockedCheck is HeroRepository.PinCheckResult.LockedOut)

    // Advance time beyond 30s lockout
    timeProvider.setTimeMillis(timeProvider.currentTimeMillis() + 31_000L)

    // Attempt 5: 60 seconds lockout
    val res5 = repository.verifyPin("999999")
    assertTrue(res5 is HeroRepository.PinCheckResult.IncorrectPin)
    val inc5 = res5 as HeroRepository.PinCheckResult.IncorrectPin
    assertEquals(5, inc5.failedAttempts)
    assertTrue("Lockout must be between 50 and 60 seconds", inc5.lockoutSecondsRemaining in 50..60)

    // Advance time beyond 60s lockout
    timeProvider.setTimeMillis(timeProvider.currentTimeMillis() + 61_000L)

    // Attempt 6: 300 seconds (5 minutes) lockout
    val res6 = repository.verifyPin("999999")
    assertTrue(res6 is HeroRepository.PinCheckResult.IncorrectPin)
    val inc6 = res6 as HeroRepository.PinCheckResult.IncorrectPin
    assertEquals(6, inc6.failedAttempts)
    assertTrue("Lockout must be between 280 and 300 seconds", inc6.lockoutSecondsRemaining in 280..300)

    // Advance time beyond 300s
    timeProvider.setTimeMillis(timeProvider.currentTimeMillis() + 301_000L)

    // Correct PIN resets failed attempts
    val successRes = repository.verifyPin("123456")
    assertTrue(successRes is HeroRepository.PinCheckResult.Success)

    val secRow = dao.getParentSecurity()
    assertNotNull(secRow)
    assertEquals(0, secRow!!.failedAttempts)
    assertEquals(0L, secRow.lockoutUntilMillis)
  }

  // ==========================================
  // 4. Acceptance Test: Real PIN Authentication & Session Protection
  // ==========================================

  @Test
  fun `test parent operations require real authenticated PIN session without bypass`() = runBlocking {
    assertFalse(repository.isParentSessionActive())

    try {
      repository.createChildProfile("علي", AgeGroup.AGE_7_9, "avatar_star")
      fail("Must throw SecurityException when session is unauthenticated")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة مصادق عليها"))
    }

    val setupOk = repository.setupInitialPin("123456")
    assertTrue(setupOk)
    assertTrue(repository.isParentSessionActive())

    val childId = repository.createChildProfile("علي", AgeGroup.AGE_7_9, "avatar_star")
    assertNotNull(childId)

    repository.lockParentSession()
    assertFalse(repository.isParentSessionActive())

    try {
      repository.createParentTask(
        title = "مهمة جديدة",
        description = "وصف المهمة",
        requiresApproval = false,
        recurrenceType = RecurrenceType.DAILY,
        targetDaysOfWeek = emptyList(),
        assignedChildIds = listOf(childId)
      )
      fail("Must throw SecurityException after session lock")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة مصادق عليها"))
    }

    val verifyRes = repository.verifyPin("123456")
    assertTrue(verifyRes is HeroRepository.PinCheckResult.Success)
    assertTrue(repository.isParentSessionActive())
  }

  // ==========================================
  // 5. Acceptance Test: Child Data Isolation
  // ==========================================

  @Test
  fun `test child profile and task occurrence data isolation`() = runBlocking {
    repository.setupInitialPin("123456")

    val child1Id = repository.createChildProfile("بطل القراءة", AgeGroup.AGE_7_9, "avatar_star")
    val child2Id = repository.createChildProfile("فارس العطاء", AgeGroup.AGE_4_6, "avatar_falcon")

    assertNotEquals(child1Id, child2Id)

    val task1Id = repository.createParentTask(
      title = "قراءة قصة مفيدة",
      description = "قراءة صفحتين بهدوء",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )

    val task2Id = repository.createParentTask(
      title = "مساعدة الأخوة",
      description = "مشاركة الألعاب بلطف",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child2Id),
      startDate = "2026-09-21"
    )

    repository.syncOccurrencesForChildAndDate(child1Id, "2026-09-21")
    repository.syncOccurrencesForChildAndDate(child2Id, "2026-09-21")

    val occurrencesChild1 = repository.getOccurrencesForChild(child1Id, "2026-09-21").first()
    val occurrencesChild2 = repository.getOccurrencesForChild(child2Id, "2026-09-21").first()

    assertEquals(1, occurrencesChild1.size)
    assertEquals(task1Id, occurrencesChild1[0].taskId)
    assertEquals(child1Id, occurrencesChild1[0].childId)

    assertEquals(1, occurrencesChild2.size)
    assertEquals(task2Id, occurrencesChild2[0].taskId)
    assertEquals(child2Id, occurrencesChild2[0].childId)
  }

  // ==========================================
  // 6. Acceptance Test: submitChildTaskCompletion Atomic Verification & Rejection Cases
  // ==========================================

  @Test
  fun `test submitChildTaskCompletion validates ownership, approval requirement and state transitions atomically`() = runBlocking {
    repository.setupInitialPin("123456")

    val child1Id = repository.createChildProfile("أحمد", AgeGroup.AGE_7_9, "avatar_star")
    val child2Id = repository.createChildProfile("سارة", AgeGroup.AGE_4_6, "avatar_falcon")

    val taskApprovalId = repository.createParentTask(
      title = "ترتيب المكتب",
      description = "ترتيب الأقلام والكتب",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )

    val taskDirectId = repository.createParentTask(
      title = "شرب الماء",
      description = "شرب كأس ماء صحي",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child2Id),
      startDate = "2026-09-21"
    )

    repository.syncOccurrencesForChildAndDate(child1Id, "2026-09-21")
    repository.syncOccurrencesForChildAndDate(child2Id, "2026-09-21")

    val occChild1 = repository.getOccurrencesForChild(child1Id, "2026-09-21").first()[0]
    val occChild2 = repository.getOccurrencesForChild(child2Id, "2026-09-21").first()[0]

    // REJECTION TEST 1: Child 2 tries to submit Child 1's task (Ownership Mismatch)
    try {
      repository.submitChildTaskCompletion(occChild1.id, selectedChildId = child2Id)
      fail("Must reject task completion when childId does not match task ownership")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("لا تنتمي للطفل"))
    }

    // REJECTION TEST 2: Invalid task ID
    try {
      repository.submitChildTaskCompletion("invalid_task_id", selectedChildId = child1Id)
      fail("Must reject submission for non-existent task")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("غير موجود"))
    }

    // SUCCESSFUL SUBMISSION: Child 1 submits task requiring approval -> must transition to PENDING_APPROVAL
    val submitted1 = repository.submitChildTaskCompletion(occChild1.id, selectedChildId = child1Id)
    assertEquals(TaskOccurrenceStatus.PENDING_APPROVAL.code, submitted1.status)
    assertTrue(submitted1.requiresApprovalSnapshot)

    // REJECTION TEST 3: Cannot submit an already pending task again
    try {
      repository.submitChildTaskCompletion(occChild1.id, selectedChildId = child1Id)
      fail("Must reject submission of task that is already pending approval")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("قيد الانتظار"))
    }

    // Parent approves task
    repository.approveTaskOccurrence(occChild1.id)
    val approvedOcc = repository.getOccurrencesForChild(child1Id, "2026-09-21").first()[0]
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, approvedOcc.status)

    // REJECTION TEST 4: Cannot submit an already COMPLETED task
    try {
      repository.submitChildTaskCompletion(occChild1.id, selectedChildId = child1Id)
      fail("Must reject modification of an already completed task")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("مكتملة بالفعل"))
    }

    // SUCCESSFUL SUBMISSION: Child 2 submits task with NO approval requirement -> transitions directly to COMPLETED
    val submitted2 = repository.submitChildTaskCompletion(occChild2.id, selectedChildId = child2Id)
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, submitted2.status)
  }

  // ==========================================
  // 7. Acceptance Test: requiresApprovalSnapshot Immutability (Requirement 4)
  // ==========================================

  @Test
  fun `test editing parent task does not alter requiresApprovalSnapshot of existing occurrence and completion obeys saved snapshot`() = runBlocking {
    repository.setupInitialPin("123456")

    val childId = repository.createChildProfile("فاطمة", AgeGroup.AGE_7_9, "avatar_star")

    // Parent creates task with requiresApproval = true
    val taskId = repository.createParentTask(
      title = "قراءة سورة الفجر",
      description = "قراءة بهدوء وتدبر",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId),
      startDate = "2026-09-21"
    )

    // Sync occurrence for today
    repository.syncOccurrencesForChildAndDate(childId, "2026-09-21")
    val initialOcc = repository.getOccurrencesForChild(childId, "2026-09-21").first()[0]
    assertTrue("Initial occurrence snapshot must require approval", initialOcc.requiresApprovalSnapshot)

    // Parent edits the parent task later and turns OFF approval requirement for future tasks
    repository.updateParentTask(
      id = taskId,
      title = "قراءة سورة الفجر",
      description = "قراءة بهدوء وتدبر",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId)
    )

    // Verify parent task updated
    val updatedTask = dao.getParentTaskById(taskId)
    assertNotNull(updatedTask)
    assertFalse("Parent task currently has requiresApproval = false", updatedTask!!.requiresApproval)

    // Existing occurrence snapshot MUST remain true
    val occBeforeSubmit = dao.getOccurrenceById(initialOcc.id)
    assertNotNull(occBeforeSubmit)
    assertTrue("Existing occurrence snapshot must not be mutated by parent task edit", occBeforeSubmit!!.requiresApprovalSnapshot)

    // Child completes the task -> must transition to PENDING_APPROVAL according to its saved snapshot, NOT COMPLETED!
    val submitted = repository.submitChildTaskCompletion(initialOcc.id, selectedChildId = childId)
    assertEquals("Must transition to PENDING_APPROVAL based on saved snapshot", TaskOccurrenceStatus.PENDING_APPROVAL.code, submitted.status)
  }

  // ==========================================
  // 8. Acceptance Test: Atomic Transitions for cancel and skip (Requirement 5)
  // ==========================================

  @Test
  fun `test cancelChildTaskPendingApproval rejects invalid child and completed task`() = runBlocking {
    repository.setupInitialPin("123456")

    val child1Id = repository.createChildProfile("عمر", AgeGroup.AGE_7_9, "avatar_falcon")
    val child2Id = repository.createChildProfile("زيد", AgeGroup.AGE_7_9, "avatar_shield")

    val taskId = repository.createParentTask(
      title = "إماطة الأذى",
      description = "تنظيف المكان بلطف",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )

    repository.syncOccurrencesForChildAndDate(child1Id, "2026-09-21")
    val occ = repository.getOccurrencesForChild(child1Id, "2026-09-21").first()[0]

    // 1. Cannot cancel when NOT_STARTED (must be in PENDING_APPROVAL)
    try {
      repository.cancelChildTaskPendingApproval(occ.id, selectedChildId = child1Id)
      fail("Must reject cancel when task is NOT in PENDING_APPROVAL")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("ليست قيد الانتظار"))
    }

    // Submit task so it becomes PENDING_APPROVAL
    repository.submitChildTaskCompletion(occ.id, selectedChildId = child1Id)

    // 2. Reject cancel if another child attempts it (Ownership mismatch)
    try {
      repository.cancelChildTaskPendingApproval(occ.id, selectedChildId = child2Id)
      fail("Must reject cancel when childId does not match")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("لا تنتمي للطفل"))
    }

    // 3. Successful cancel transitions back to NOT_STARTED
    repository.cancelChildTaskPendingApproval(occ.id, selectedChildId = child1Id)
    val cancelledOcc = dao.getOccurrenceById(occ.id)
    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, cancelledOcc!!.status)

    // Approve task to make it COMPLETED
    repository.submitChildTaskCompletion(occ.id, selectedChildId = child1Id)
    repository.approveTaskOccurrence(occ.id)

    // 4. Reject cancel on COMPLETED task
    try {
      repository.cancelChildTaskPendingApproval(occ.id, selectedChildId = child1Id)
      fail("Must reject cancel on already completed task")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("مكتملة بالفعل"))
    }
  }

  @Test
  fun `test skipTaskToday rejects invalid child and completed task`() = runBlocking {
    repository.setupInitialPin("123456")

    val child1Id = repository.createChildProfile("نور", AgeGroup.AGE_4_6, "avatar_star")
    val child2Id = repository.createChildProfile("سما", AgeGroup.AGE_4_6, "avatar_flower")

    val taskId = repository.createParentTask(
      title = "ترتيب الألعاب",
      description = "إعادة الألعاب لصندوقها",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )

    repository.syncOccurrencesForChildAndDate(child1Id, "2026-09-21")
    val occ = repository.getOccurrencesForChild(child1Id, "2026-09-21").first()[0]

    // 1. Reject skip if another child attempts it
    try {
      repository.skipTaskToday(occ.id, selectedChildId = child2Id)
      fail("Must reject skip when childId does not match")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("لا تنتمي للطفل"))
    }

    // 2. Successful skip transitions to SKIPPED
    repository.skipTaskToday(occ.id, selectedChildId = child1Id)
    val skippedOcc = dao.getOccurrenceById(occ.id)
    assertEquals(TaskOccurrenceStatus.SKIPPED.code, skippedOcc!!.status)

    // 3. Reject skip on COMPLETED task
    val completedTaskId = repository.createParentTask(
      title = "الوضوء للصلاة",
      description = "الوضوء بنشاط",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )
    val completedOcc = repository.submitChildTaskCompletion(completedTaskId, selectedChildId = child1Id)
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, completedOcc.status)

    try {
      repository.skipTaskToday(completedOcc.id, selectedChildId = child1Id)
      fail("Must reject skip on completed task")
    } catch (e: IllegalStateException) {
      assertTrue(e.message!!.contains("مكتملة بالفعل"))
    }
  }

  // ==========================================
  // 9. Acceptance Test: Error Message Surfacing in ViewModel (Requirement 6)
  // ==========================================

  @Test
  fun `test submitTaskCompletion failure exposes meaningful error message in ViewModel without swallowing`() = runBlocking {
    repository.setupInitialPin("123456")

    val childId = repository.createChildProfile("خالد", AgeGroup.AGE_7_9, "avatar_star")

    val viewModel = HeroViewModel(
      application = ApplicationProvider.getApplicationContext(),
      customRepository = repository,
      timeProvider = timeProvider
    )

    // With no child selected, submitTaskCompletion sets error message
    assertNull(viewModel.errorMessage.value)
    viewModel.submitTaskCompletion("invalid_id")
    assertNotNull(viewModel.errorMessage.value)
    assertTrue(viewModel.errorMessage.value!!.contains("اختيار ملف الطفل"))

    viewModel.dismissErrorMessage()
    assertNull(viewModel.errorMessage.value)

    // Select child and attempt non-existent task ID
    viewModel.selectChild(childId)
    viewModel.submitTaskCompletion("non_existent_occurrence_123")

    // Error message must be populated with meaningful text
    assertNotNull(viewModel.errorMessage.value)
    assertTrue(viewModel.errorMessage.value!!.contains("غير موجود"))
  }

  // ==========================================
  // 10. Acceptance Test: Reactive Date Handling & Midnight Rollover
  // ==========================================

  @Test
  fun `test reactive date handling updates occurrences across midnight and on refresh without changing child`() = runBlocking {
    repository.setupInitialPin("123456")

    val childId = repository.createChildProfile("بطل اليوم", AgeGroup.AGE_7_9, "avatar_lion")

    val taskId = repository.createParentTask(
      title = "صلاة الفجر في وقتها",
      description = "الاستيقاظ بنشاط",
      requiresApproval = false,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(childId),
      startDate = "2026-09-21"
    )

    val viewModel = HeroViewModel(
      application = ApplicationProvider.getApplicationContext(),
      customRepository = repository,
      timeProvider = timeProvider
    )

    viewModel.selectChild(childId)
    viewModel.syncTodayTasksForChild(childId)

    val occDay1 = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(1, occDay1.size)
    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, occDay1[0].status)

    viewModel.submitTaskCompletion(occDay1[0].id)
    val occDay1Completed = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, occDay1Completed[0].status)

    timeProvider.setDate("2026-09-22")
    viewModel.refreshTodayDate()

    assertEquals("2026-09-22", viewModel.currentDateString.value)

    val occDay2 = repository.getOccurrencesForChild(childId, "2026-09-22").first()
    assertEquals(1, occDay2.size)
    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, occDay2[0].status)
    assertEquals("2026-09-22", occDay2[0].dateStr)

    val occDay1Check = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, occDay1Check[0].status)
  }

  // ==========================================
  // 11. Acceptance Test: Real v1 to v3 Database Migration via Room (Requirement 2)
  // ==========================================

  @Test
  fun `test database migration from real v1 schema preserves all historical tables and opens cleanly in Room`() = runBlocking {
    val dbName = "real_v1_migration_test.db"
    context.deleteDatabase(dbName)

    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(dbName)
      .callback(object : SupportSQLiteOpenHelper.Callback(1) {
        override fun onCreate(db: SupportSQLiteDatabase) {
          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `honored_heroes` (
              `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              `name` TEXT NOT NULL,
              `relation` TEXT NOT NULL,
              `reason` TEXT NOT NULL,
              `badgeName` TEXT NOT NULL,
              `dateMillis` INTEGER NOT NULL
            )
          """.trimIndent())

          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `daily_deeds_progress` (
              `deedKey` TEXT NOT NULL PRIMARY KEY,
              `dateStr` TEXT NOT NULL,
              `deedId` TEXT NOT NULL,
              `isCompleted` INTEGER NOT NULL,
              `completedAtMillis` INTEGER NOT NULL
            )
          """.trimIndent())

          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `favorite_heroes` (
              `heroId` TEXT NOT NULL PRIMARY KEY,
              `addedAtMillis` INTEGER NOT NULL
            )
          """.trimIndent())
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
      })
      .build()

    val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
    val dbSqlite = openHelper.writableDatabase

    dbSqlite.execSQL("""
      INSERT INTO `honored_heroes` (`name`, `relation`, `reason`, `badgeName`, `dateMillis`)
      VALUES ('أبي العزيز', 'أب ومربي', 'تعليمه لي الصدق والمثابرة', 'وسام الحكمة', 1726800000000)
    """.trimIndent())

    dbSqlite.execSQL("""
      INSERT INTO `daily_deeds_progress` (`deedKey`, `dateStr`, `deedId`, `isCompleted`, `completedAtMillis`)
      VALUES ('2026-09-20_smile', '2026-09-20', 'smile', 1, 1726800000000)
    """.trimIndent())

    dbSqlite.execSQL("""
      INSERT INTO `favorite_heroes` (`heroId`, `addedAtMillis`)
      VALUES ('al_khwarizmi', 1726800000000)
    """.trimIndent())

    dbSqlite.close()

    val migratedRoomDb = Room.databaseBuilder(context, HeroDatabase::class.java, dbName)
      .addMigrations(HeroDatabase.MIGRATION_1_2, HeroDatabase.MIGRATION_2_3)
      .allowMainThreadQueries()
      .build()

    val migratedDao = migratedRoomDb.heroDao()

    val honoredList = migratedDao.getAllHonoredHeroes().first()
    assertEquals(1, honoredList.size)
    assertEquals("أبي العزيز", honoredList[0].name)
    assertEquals("وسام الحكمة", honoredList[0].badgeName)

    val favorites = migratedDao.getAllFavorites().first()
    assertEquals(1, favorites.size)
    assertEquals("al_khwarizmi", favorites[0].heroId)

    // Solves Requirement 2 using valid DAO query
    val deedProgress = migratedDao.getDeedProgress("2026-09-20_smile")
    assertNotNull(deedProgress)
    assertTrue(deedProgress!!.isCompleted)

    val children = migratedDao.getActiveChildren().first()
    assertTrue(children.isEmpty())

    val sec = migratedDao.getParentSecurity()
    assertNull("Security should not be configured yet", sec)

    migratedRoomDb.close()
    context.deleteDatabase(dbName)
  }

  // ==========================================
  // 12. Acceptance Test: Real v2 to v3 Database Migration with All Indexes (Requirement 3)
  // ==========================================

  @Test
  fun `test database migration from real v2 schema with all indexes upgrades to v3 and preserves data in Room`() = runBlocking {
    val dbName = "real_v2_to_v3_migration_test.db"
    context.deleteDatabase(dbName)

    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(dbName)
      .callback(object : SupportSQLiteOpenHelper.Callback(2) {
        override fun onCreate(db: SupportSQLiteDatabase) {
          // v1 tables
          db.execSQL("CREATE TABLE IF NOT EXISTS `honored_heroes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `relation` TEXT NOT NULL, `reason` TEXT NOT NULL, `badgeName` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `daily_deeds_progress` (`deedKey` TEXT NOT NULL PRIMARY KEY, `dateStr` TEXT NOT NULL, `deedId` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `completedAtMillis` INTEGER NOT NULL)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `favorite_heroes` (`heroId` TEXT NOT NULL PRIMARY KEY, `addedAtMillis` INTEGER NOT NULL)")

          // v2 tables matching actual schema exactly including indexes
          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `child_profiles` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `alias` TEXT NOT NULL,
              `ageGroup` TEXT NOT NULL,
              `avatarId` TEXT NOT NULL,
              `createdAtMillis` INTEGER NOT NULL,
              `isArchived` INTEGER NOT NULL
            )
          """.trimIndent())

          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `parent_tasks` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `title` TEXT NOT NULL,
              `description` TEXT NOT NULL,
              `requiresApproval` INTEGER NOT NULL,
              `recurrenceType` TEXT NOT NULL,
              `targetDaysOfWeek` TEXT NOT NULL,
              `startDate` TEXT NOT NULL,
              `isArchived` INTEGER NOT NULL,
              `createdAtMillis` INTEGER NOT NULL
            )
          """.trimIndent())

          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `task_assignments` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `taskId` TEXT NOT NULL,
              `childId` TEXT NOT NULL,
              `createdAtMillis` INTEGER NOT NULL
            )
          """.trimIndent())
          db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_task_assignments_taskId_childId` ON `task_assignments` (`taskId`, `childId`)")
          db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_assignments_childId` ON `task_assignments` (`childId`)")

          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `task_occurrences` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `taskId` TEXT NOT NULL,
              `childId` TEXT NOT NULL,
              `dateStr` TEXT NOT NULL,
              `status` TEXT NOT NULL,
              `snapshotTitle` TEXT NOT NULL,
              `snapshotDescription` TEXT NOT NULL,
              `requiresApprovalSnapshot` INTEGER NOT NULL,
              `completedAtMillis` INTEGER,
              `parentFeedbackNote` TEXT,
              `updatedAtMillis` INTEGER NOT NULL
            )
          """.trimIndent())
          db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_task_occurrences_taskId_childId_dateStr` ON `task_occurrences` (`taskId`, `childId`, `dateStr`)")
          db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_occurrences_childId_dateStr` ON `task_occurrences` (`childId`, `dateStr`)")

          db.execSQL("""
            CREATE TABLE IF NOT EXISTS `parent_security` (
              `id` INTEGER NOT NULL PRIMARY KEY,
              `pinSalt` TEXT NOT NULL,
              `pinHash` TEXT NOT NULL,
              `failedAttempts` INTEGER NOT NULL,
              `lockoutUntilMillis` INTEGER NOT NULL,
              `isConfigured` INTEGER NOT NULL,
              `updatedAtMillis` INTEGER NOT NULL
            )
          """.trimIndent())
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
      })
      .build()

    val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
    val dbSqlite = openHelper.writableDatabase

    // Insert representative data across all v2 tables
    dbSqlite.execSQL("INSERT INTO `parent_security` VALUES (1, 'salt_v2_exact', 'hash_v2_exact', 0, 0, 1, 1726800000000)")
    dbSqlite.execSQL("INSERT INTO `child_profiles` VALUES ('child_10', 'حمزة', 'AGE_7_9', 'avatar_lion', 1726800000000, 0)")
    dbSqlite.execSQL("INSERT INTO `parent_tasks` VALUES ('task_10', 'بر الوالدين', 'مساعدة أمي', 1, 'DAILY', '', '2026-09-20', 0, 1726800000000)")
    dbSqlite.execSQL("INSERT INTO `task_assignments` VALUES ('assign_10', 'task_10', 'child_10', 1726800000000)")
    dbSqlite.execSQL("INSERT INTO `task_occurrences` VALUES ('occ_10', 'task_10', 'child_10', '2026-09-20', 'COMPLETED', 'بر الوالدين', 'مساعدة أمي', 1, 1726800100000, NULL, 1726800100000)")

    dbSqlite.close()

    // Open with Room using MIGRATION_2_3
    val migratedRoomDb = Room.databaseBuilder(context, HeroDatabase::class.java, dbName)
      .addMigrations(HeroDatabase.MIGRATION_2_3)
      .allowMainThreadQueries()
      .build()

    val migratedDao = migratedRoomDb.heroDao()

    // Verify parent security upgraded to v3 columns with defaults
    val sec = migratedDao.getParentSecurity()
    assertNotNull(sec)
    assertEquals("salt_v2_exact", sec!!.pinSalt)
    assertEquals("hash_v2_exact", sec.pinHash)
    assertEquals(1, sec.algoVersion)
    assertEquals(10000, sec.iterations)
    assertEquals("PBKDF2WithHmacSHA256", sec.algorithm)

    // Verify child profiles intact
    val children = migratedDao.getActiveChildren().first()
    assertEquals(1, children.size)
    assertEquals("حمزة", children[0].alias)

    // Verify parent tasks intact
    val tasks = migratedDao.getAllTasks().first()
    assertEquals(1, tasks.size)
    assertEquals("بر الوالدين", tasks[0].title)

    // Verify occurrences intact
    val occurrences = migratedDao.getOccurrencesForChildAndDate("child_10", "2026-09-20").first()
    assertEquals(1, occurrences.size)
    assertEquals("occ_10", occurrences[0].id)
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, occurrences[0].status)

    migratedRoomDb.close()
    context.deleteDatabase(dbName)
  }

  // ==========================================
  // 13. Acceptance Test: Legacy PIN Verification & Transparent Migration
  // ==========================================

  @Test
  fun `test legacy PBKDF2 pin from v2 is verified and upgraded to current algorithm version`() = runBlocking {
    val pin = "123456"
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(pin, salt, algorithm = SecurityUtils.DEFAULT_ALGORITHM, iterations = 10_000)

    val legacyEntity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = hash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      algoVersion = SecurityUtils.LEGACY_ALGO_VERSION_1,
      iterations = 10_000,
      algorithm = SecurityUtils.DEFAULT_ALGORITHM,
      updatedAtMillis = 1726800000000L
    )
    dao.insertOrUpdateParentSecurity(legacyEntity)

    val checkRes = repository.verifyPin(pin)
    assertTrue("Legacy PBKDF2 PIN must verify successfully", checkRes is HeroRepository.PinCheckResult.Success)

    val upgraded = dao.getParentSecurity()
    assertNotNull(upgraded)
    assertEquals(SecurityUtils.CURRENT_ALGO_VERSION, upgraded!!.algoVersion)
    assertEquals(SecurityUtils.DEFAULT_ALGORITHM, upgraded.algorithm)
    assertEquals(10_000, upgraded.iterations)

    val secondLogin = repository.verifyPin(pin)
    assertTrue(secondLogin is HeroRepository.PinCheckResult.Success)
  }

  @Test
  fun `test legacy SHA256_MULTI_ROUND pin from v2 is verified and upgraded to PBKDF2`() = runBlocking {
    val pin = "654321"
    val salt = SecurityUtils.generateSalt()
    val legacyShaHash = SecurityUtils.hashPin(
      pin = pin,
      saltBase64 = salt,
      algorithm = SecurityUtils.LEGACY_ALGORITHM_SHA256_MULTI,
      iterations = 10_000
    )

    val legacyEntity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = legacyShaHash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      algoVersion = SecurityUtils.LEGACY_ALGO_VERSION_1,
      iterations = 10_000,
      algorithm = SecurityUtils.DEFAULT_ALGORITHM,
      updatedAtMillis = 1726800000000L
    )
    dao.insertOrUpdateParentSecurity(legacyEntity)

    val wrongRes = repository.verifyPin("000000")
    assertTrue(wrongRes is HeroRepository.PinCheckResult.IncorrectPin)

    val checkRes = repository.verifyPin(pin)
    assertTrue("Legacy SHA256_MULTI_ROUND PIN must verify successfully", checkRes is HeroRepository.PinCheckResult.Success)

    val upgraded = dao.getParentSecurity()
    assertNotNull(upgraded)
    assertEquals(SecurityUtils.CURRENT_ALGO_VERSION, upgraded!!.algoVersion)
    assertEquals(SecurityUtils.DEFAULT_ALGORITHM, upgraded.algorithm)
    assertEquals(10_000, upgraded.iterations)

    assertTrue(SecurityUtils.verifyPin(pin, upgraded.pinSalt, upgraded.pinHash, SecurityUtils.DEFAULT_ALGORITHM, 10_000))

    val secondLogin = repository.verifyPin(pin)
    assertTrue(secondLogin is HeroRepository.PinCheckResult.Success)
  }

  // ==========================================
  // 14. Acceptance Test: Educational Praise and Tone
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
