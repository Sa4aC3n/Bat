package com.batal.elyoum

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
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
  // 1. Acceptance Test: SecurityUtils & PIN Format
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
  // 2. Acceptance Test: PIN Rate Limiting & Lockout
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
  // 3. Acceptance Test: Real PIN Authentication & Session Protection
  // ==========================================

  @Test
  fun `test parent operations require real authenticated PIN session without bypass`() = runBlocking {
    // Before PIN setup/verification, parent session is inactive
    assertFalse(repository.isParentSessionActive())

    // Attempting parent operations without authentication throws SecurityException
    try {
      repository.createChildProfile("علي", AgeGroup.AGE_7_9, "avatar_star")
      fail("Must throw SecurityException when session is unauthenticated")
    } catch (e: SecurityException) {
      assertTrue(e.message!!.contains("جلسة مصادق عليها"))
    }

    // Setting up the initial PIN authenticates the session
    val setupOk = repository.setupInitialPin("123456")
    assertTrue(setupOk)
    assertTrue(repository.isParentSessionActive())

    // Now parent operations succeed
    val childId = repository.createChildProfile("علي", AgeGroup.AGE_7_9, "avatar_star")
    assertNotNull(childId)

    // Locking the session de-authenticates it
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

    // Authenticating via real verifyPin restores session
    val verifyRes = repository.verifyPin("123456")
    assertTrue(verifyRes is HeroRepository.PinCheckResult.Success)
    assertTrue(repository.isParentSessionActive())
  }

  // ==========================================
  // 4. Acceptance Test: Child Data Isolation
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
  // 5. Acceptance Test: submitChildTaskCompletion Atomic Verification & Rejection Cases
  // ==========================================

  @Test
  fun `test submitChildTaskCompletion validates ownership, approval requirement and state transitions atomically`() = runBlocking {
    repository.setupInitialPin("123456")

    val child1Id = repository.createChildProfile("أحمد", AgeGroup.AGE_7_9, "avatar_star")
    val child2Id = repository.createChildProfile("سارة", AgeGroup.AGE_4_6, "avatar_falcon")

    // Task requiring approval for child1
    val taskApprovalId = repository.createParentTask(
      title = "ترتيب المكتب",
      description = "ترتيب الأقلام والكتب",
      requiresApproval = true,
      recurrenceType = RecurrenceType.DAILY,
      targetDaysOfWeek = emptyList(),
      assignedChildIds = listOf(child1Id),
      startDate = "2026-09-21"
    )

    // Task NOT requiring approval for child2
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
  // 6. Acceptance Test: Reactive Date Handling & Midnight Rollover
  // ==========================================

  @Test
  fun `test reactive date handling updates occurrences across midnight and on refresh without changing child`() = runBlocking {
    repository.setupInitialPin("123456")

    val childId = repository.createChildProfile("بطل اليوم", AgeGroup.AGE_7_9, "avatar_lion")

    // Create a daily task starting 2026-09-21
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

    // On 2026-09-21: occurrence exists and child completes it
    val occDay1 = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(1, occDay1.size)
    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, occDay1[0].status)

    viewModel.submitTaskCompletion(occDay1[0].id)
    val occDay1Completed = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, occDay1Completed[0].status)

    // ADVANCE TO MIDNIGHT / NEXT DAY: 2026-09-22 without changing selectedChild
    timeProvider.setDate("2026-09-22")

    // Calling refreshTodayDate (which occurs on app resume or midnight tick)
    viewModel.refreshTodayDate()

    // Verify ViewModel date state updated
    assertEquals("2026-09-22", viewModel.currentDateString.value)

    // Verify that the task occurrences for the NEW date are fresh (NOT_STARTED) and independent of previous day
    val occDay2 = repository.getOccurrencesForChild(childId, "2026-09-22").first()
    assertEquals(1, occDay2.size)
    assertEquals(TaskOccurrenceStatus.NOT_STARTED.code, occDay2[0].status)
    assertEquals("2026-09-22", occDay2[0].dateStr)

    // Previous day occurrence remains safely COMPLETED
    val occDay1Check = repository.getOccurrencesForChild(childId, "2026-09-21").first()
    assertEquals(TaskOccurrenceStatus.COMPLETED.code, occDay1Check[0].status)
  }

  // ==========================================
  // 7. Acceptance Test: Real v1 to v3 & v2 to v3 Database Migrations via Room
  // ==========================================

  @Test
  fun `test database migration from real v1 schema preserves all historical tables and opens cleanly in Room`() = runBlocking {
    val dbName = "real_v1_migration_test.db"
    context.deleteDatabase(dbName)

    // 1. Create SQLite DB with actual v1 schema (honored_heroes, daily_deeds_progress, favorite_heroes)
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

    // Insert representative data into all 3 v1 tables
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

    // 2. Open migrated database directly with Room using official MIGRATION_1_2 and MIGRATION_2_3
    val migratedRoomDb = Room.databaseBuilder(context, HeroDatabase::class.java, dbName)
      .addMigrations(HeroDatabase.MIGRATION_1_2, HeroDatabase.MIGRATION_2_3)
      .allowMainThreadQueries()
      .build()

    val migratedDao = migratedRoomDb.heroDao()

    // Verify all original v1 data is accessible through Room entities
    val honoredList = migratedDao.getAllHonoredHeroes().first()
    assertEquals(1, honoredList.size)
    assertEquals("أبي العزيز", honoredList[0].name)
    assertEquals("وسام الحكمة", honoredList[0].badgeName)

    val favorites = migratedDao.getAllFavorites().first()
    assertEquals(1, favorites.size)
    assertEquals("al_khwarizmi", favorites[0].heroId)

    val deedProgress = migratedDao.getDeedProgress("2026-09-20_smile")
    assertNotNull(deedProgress)
    assertTrue(deedProgress!!.isCompleted)

    // Verify new v3 tables are ready and operational
    val children = migratedDao.getActiveChildren().first()
    assertTrue(children.isEmpty())

    val sec = migratedDao.getParentSecurity()
    assertNull("Security should not be configured yet", sec)

    migratedRoomDb.close()
    context.deleteDatabase(dbName)
  }

  @Test
  fun `test database migration from v2 to v3 upgrades security schema and preserves tasks`() = runBlocking {
    val dbName = "v2_to_v3_migration_test.db"
    context.deleteDatabase(dbName)

    val helperConfig = SupportSQLiteOpenHelper.Configuration.builder(context)
      .name(dbName)
      .callback(object : SupportSQLiteOpenHelper.Callback(2) {
        override fun onCreate(db: SupportSQLiteDatabase) {
          // All v1 tables
          db.execSQL("CREATE TABLE IF NOT EXISTS `honored_heroes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `relation` TEXT NOT NULL, `reason` TEXT NOT NULL, `badgeName` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `daily_deeds_progress` (`deedKey` TEXT NOT NULL PRIMARY KEY, `dateStr` TEXT NOT NULL, `deedId` TEXT NOT NULL, `isCompleted` INTEGER NOT NULL, `completedAtMillis` INTEGER NOT NULL)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `favorite_heroes` (`heroId` TEXT NOT NULL PRIMARY KEY, `addedAtMillis` INTEGER NOT NULL)")

          // v2 tables
          db.execSQL("CREATE TABLE IF NOT EXISTS `child_profiles` (`id` TEXT NOT NULL PRIMARY KEY, `alias` TEXT NOT NULL, `ageGroup` TEXT NOT NULL, `avatarId` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL, `isArchived` INTEGER NOT NULL DEFAULT 0)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `parent_tasks` (`id` TEXT NOT NULL PRIMARY KEY, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `requiresApproval` INTEGER NOT NULL, `recurrenceType` TEXT NOT NULL, `targetDaysOfWeek` TEXT NOT NULL, `startDate` TEXT NOT NULL, `isArchived` INTEGER NOT NULL DEFAULT 0, `createdAtMillis` INTEGER NOT NULL)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `task_assignments` (`id` TEXT NOT NULL PRIMARY KEY, `taskId` TEXT NOT NULL, `childId` TEXT NOT NULL, `createdAtMillis` INTEGER NOT NULL)")
          db.execSQL("CREATE TABLE IF NOT EXISTS `task_occurrences` (`id` TEXT NOT NULL PRIMARY KEY, `taskId` TEXT NOT NULL, `childId` TEXT NOT NULL, `dateStr` TEXT NOT NULL, `status` TEXT NOT NULL, `snapshotTitle` TEXT NOT NULL, `snapshotDescription` TEXT NOT NULL, `requiresApprovalSnapshot` INTEGER NOT NULL, `completedAtMillis` INTEGER, `parentFeedbackNote` TEXT, `updatedAtMillis` INTEGER NOT NULL)")
          // v2 parent_security without algoVersion, iterations, algorithm
          db.execSQL("CREATE TABLE IF NOT EXISTS `parent_security` (`id` INTEGER NOT NULL PRIMARY KEY, `pinSalt` TEXT NOT NULL, `pinHash` TEXT NOT NULL, `failedAttempts` INTEGER NOT NULL, `lockoutUntilMillis` INTEGER NOT NULL, `isConfigured` INTEGER NOT NULL, `updatedAtMillis` INTEGER NOT NULL)")
        }

        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
      })
      .build()

    val openHelper = FrameworkSQLiteOpenHelperFactory().create(helperConfig)
    val dbSqlite = openHelper.writableDatabase

    // Insert representative v2 security & child record
    dbSqlite.execSQL("""
      INSERT INTO `parent_security` VALUES (1, 'salt_v2', 'hash_v2', 0, 0, 1, 1726800000000)
    """.trimIndent())

    dbSqlite.execSQL("""
      INSERT INTO `child_profiles` VALUES ('child_1', 'يوسف', 'AGE_7_9', 'avatar_falcon', 1726800000000, 0)
    """.trimIndent())

    dbSqlite.close()

    // Open with Room using MIGRATION_2_3
    val migratedRoomDb = Room.databaseBuilder(context, HeroDatabase::class.java, dbName)
      .addMigrations(HeroDatabase.MIGRATION_2_3)
      .allowMainThreadQueries()
      .build()

    val migratedDao = migratedRoomDb.heroDao()
    val sec = migratedDao.getParentSecurity()
    assertNotNull(sec)
    assertEquals("salt_v2", sec!!.pinSalt)
    assertEquals("hash_v2", sec.pinHash)
    assertEquals(1, sec.algoVersion) // defaulted by migration
    assertEquals(10000, sec.iterations)
    assertEquals("PBKDF2WithHmacSHA256", sec.algorithm)

    val children = migratedDao.getActiveChildren().first()
    assertEquals(1, children.size)
    assertEquals("يوسف", children[0].alias)

    migratedRoomDb.close()
    context.deleteDatabase(dbName)
  }

  // ==========================================
  // 8. Acceptance Test: Legacy PIN Verification & Transparent Migration
  // ==========================================

  @Test
  fun `test legacy PBKDF2 pin from v2 is verified and upgraded to current algorithm version`() = runBlocking {
    val pin = "123456"
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(pin, salt, algorithm = SecurityUtils.DEFAULT_ALGORITHM, iterations = 10_000)

    // Simulate record migrated from v2 (algoVersion = 1)
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

    // Verify PIN succeeds
    val checkRes = repository.verifyPin(pin)
    assertTrue("Legacy PBKDF2 PIN must verify successfully", checkRes is HeroRepository.PinCheckResult.Success)

    // Check that record was transparently upgraded to CURRENT_ALGO_VERSION
    val upgraded = dao.getParentSecurity()
    assertNotNull(upgraded)
    assertEquals(SecurityUtils.CURRENT_ALGO_VERSION, upgraded!!.algoVersion)
    assertEquals(SecurityUtils.DEFAULT_ALGORITHM, upgraded.algorithm)
    assertEquals(10_000, upgraded.iterations)

    // Subsequent logins work cleanly with upgraded record
    val secondLogin = repository.verifyPin(pin)
    assertTrue(secondLogin is HeroRepository.PinCheckResult.Success)
  }

  @Test
  fun `test legacy SHA256_MULTI_ROUND pin from v2 is verified and upgraded to PBKDF2`() = runBlocking {
    val pin = "654321"
    val salt = SecurityUtils.generateSalt()
    // Generate legacy SHA256_MULTI_ROUND hash
    val legacyShaHash = SecurityUtils.hashPin(
      pin = pin,
      saltBase64 = salt,
      algorithm = SecurityUtils.LEGACY_ALGORITHM_SHA256_MULTI,
      iterations = 10_000
    )

    // Simulate v2 record migrated to v3 where algorithm was defaulted to PBKDF2WithHmacSHA256, but hash was SHA256_MULTI_ROUND
    val legacyEntity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = legacyShaHash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      algoVersion = SecurityUtils.LEGACY_ALGO_VERSION_1,
      iterations = 10_000,
      algorithm = SecurityUtils.DEFAULT_ALGORITHM, // Default from migration
      updatedAtMillis = 1726800000000L
    )
    dao.insertOrUpdateParentSecurity(legacyEntity)

    // Wrong PIN must still fail
    val wrongRes = repository.verifyPin("000000")
    assertTrue(wrongRes is HeroRepository.PinCheckResult.IncorrectPin)

    // Correct legacy PIN must verify through legacy fallback and transparently upgrade
    val checkRes = repository.verifyPin(pin)
    assertTrue("Legacy SHA256_MULTI_ROUND PIN must verify successfully", checkRes is HeroRepository.PinCheckResult.Success)

    // Check that record was transparently upgraded to CURRENT_ALGO_VERSION with standard PBKDF2
    val upgraded = dao.getParentSecurity()
    assertNotNull(upgraded)
    assertEquals(SecurityUtils.CURRENT_ALGO_VERSION, upgraded!!.algoVersion)
    assertEquals(SecurityUtils.DEFAULT_ALGORITHM, upgraded.algorithm)
    assertEquals(10_000, upgraded.iterations)

    // The upgraded hash must now verify with PBKDF2!
    assertTrue(SecurityUtils.verifyPin(pin, upgraded.pinSalt, upgraded.pinHash, SecurityUtils.DEFAULT_ALGORITHM, 10_000))

    // Subsequent logins work directly with upgraded PBKDF2
    val secondLogin = repository.verifyPin(pin)
    assertTrue(secondLogin is HeroRepository.PinCheckResult.Success)
  }

  // ==========================================
  // 9. Acceptance Test: Educational Praise and Tone
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
