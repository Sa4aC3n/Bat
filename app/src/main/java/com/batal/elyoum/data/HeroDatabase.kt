package com.batal.elyoum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    HonoredHeroEntity::class,
    DailyDeedProgressEntity::class,
    FavoriteHeroEntity::class,
    ChildProfileEntity::class,
    ParentTaskEntity::class,
    TaskAssignmentEntity::class,
    TaskOccurrenceEntity::class,
    ParentSecurityEntity::class,
    FamilyRewardEntity::class,
    FamilyRewardHistoryEntity::class,
    ContentReviewRecordEntity::class,
    AppSettingsEntity::class
  ],
  version = 4,
  exportSchema = false
)
abstract class HeroDatabase : RoomDatabase() {
  abstract fun heroDao(): HeroDao

  companion object {
    @Volatile
    private var INSTANCE: HeroDatabase? = null

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // 1. child_profiles
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `child_profiles` (
            `id` TEXT NOT NULL,
            `alias` TEXT NOT NULL,
            `ageGroup` TEXT NOT NULL,
            `avatarId` TEXT NOT NULL,
            `createdAtMillis` INTEGER NOT NULL,
            `isArchived` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())

        // 2. parent_tasks
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `parent_tasks` (
            `id` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `description` TEXT NOT NULL,
            `requiresApproval` INTEGER NOT NULL,
            `recurrenceType` TEXT NOT NULL,
            `targetDaysOfWeek` TEXT NOT NULL,
            `startDate` TEXT NOT NULL,
            `isArchived` INTEGER NOT NULL,
            `createdAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())

        // 3. task_assignments
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `task_assignments` (
            `id` TEXT NOT NULL,
            `taskId` TEXT NOT NULL,
            `childId` TEXT NOT NULL,
            `createdAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_task_assignments_taskId_childId` ON `task_assignments` (`taskId`, `childId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_assignments_childId` ON `task_assignments` (`childId`)")

        // 4. task_occurrences
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `task_occurrences` (
            `id` TEXT NOT NULL,
            `taskId` TEXT NOT NULL,
            `childId` TEXT NOT NULL,
            `dateStr` TEXT NOT NULL,
            `status` TEXT NOT NULL,
            `snapshotTitle` TEXT NOT NULL,
            `snapshotDescription` TEXT NOT NULL,
            `requiresApprovalSnapshot` INTEGER NOT NULL,
            `completedAtMillis` INTEGER,
            `parentFeedbackNote` TEXT,
            `updatedAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_task_occurrences_taskId_childId_dateStr` ON `task_occurrences` (`taskId`, `childId`, `dateStr`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_occurrences_childId_dateStr` ON `task_occurrences` (`childId`, `dateStr`)")

        // 5. parent_security
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `parent_security` (
            `id` INTEGER NOT NULL,
            `pinSalt` TEXT NOT NULL,
            `pinHash` TEXT NOT NULL,
            `failedAttempts` INTEGER NOT NULL,
            `lockoutUntilMillis` INTEGER NOT NULL,
            `isConfigured` INTEGER NOT NULL,
            `updatedAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())
      }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `parent_security` ADD COLUMN `algoVersion` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `parent_security` ADD COLUMN `iterations` INTEGER NOT NULL DEFAULT 10000")
        db.execSQL("ALTER TABLE `parent_security` ADD COLUMN `algorithm` TEXT NOT NULL DEFAULT 'PBKDF2WithHmacSHA256'")
      }
    }

    val MIGRATION_1_3 = object : Migration(1, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        MIGRATION_1_2.migrate(db)
        MIGRATION_2_3.migrate(db)
      }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // 1. parent_tasks alterations
        db.execSQL("ALTER TABLE `parent_tasks` ADD COLUMN `ageGroup` TEXT NOT NULL DEFAULT 'ALL'")
        db.execSQL("ALTER TABLE `parent_tasks` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'GENERAL'")

        // 2. task_occurrences alterations
        db.execSQL("ALTER TABLE `task_occurrences` ADD COLUMN `postponedToDate` TEXT")
        db.execSQL("ALTER TABLE `task_occurrences` ADD COLUMN `originalOccurrenceId` TEXT")

        // 3. family_rewards
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `family_rewards` (
            `id` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `description` TEXT NOT NULL,
            `rewardType` TEXT NOT NULL,
            `childId` TEXT NOT NULL,
            `grantMode` TEXT NOT NULL,
            `goalCriteria` TEXT,
            `targetDate` TEXT,
            `status` TEXT NOT NULL,
            `cancellationReason` TEXT,
            `requestedAtMillis` INTEGER,
            `fulfilledAtMillis` INTEGER,
            `createdAtMillis` INTEGER NOT NULL,
            `updatedAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_family_rewards_childId` ON `family_rewards` (`childId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_family_rewards_status` ON `family_rewards` (`status`)")

        // 4. family_reward_history
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `family_reward_history` (
            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `rewardId` TEXT NOT NULL,
            `fromStatus` TEXT NOT NULL,
            `toStatus` TEXT NOT NULL,
            `note` TEXT,
            `timestampMillis` INTEGER NOT NULL
          )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_family_reward_history_rewardId` ON `family_reward_history` (`rewardId`)")

        // 5. content_review_records
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `content_review_records` (
            `contentId` TEXT NOT NULL,
            `contentVersion` INTEGER NOT NULL,
            `title` TEXT NOT NULL,
            `text` TEXT NOT NULL,
            `ageGroup` TEXT NOT NULL,
            `targetGoal` TEXT NOT NULL,
            `materialType` TEXT NOT NULL,
            `sourceReferenceTitle` TEXT NOT NULL,
            `sourceAuthorOrEntity` TEXT NOT NULL,
            `sourceUrl` TEXT,
            `sourceCitationLocation` TEXT,
            `reviewStatus` TEXT NOT NULL,
            `reviewerName` TEXT,
            `reviewerSpecialty` TEXT,
            `reviewDate` TEXT,
            `approvalScope` TEXT,
            `reviewedVersion` INTEGER,
            `internalProofReference` TEXT,
            `createdAtMillis` INTEGER NOT NULL,
            `updatedAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`contentId`)
          )
        """.trimIndent())

        // 6. app_settings
        db.execSQL("""
          CREATE TABLE IF NOT EXISTS `app_settings` (
            `id` INTEGER NOT NULL,
            `themeMode` TEXT NOT NULL,
            `notificationsEnabled` INTEGER NOT NULL,
            `notificationQuietHourStart` INTEGER NOT NULL,
            `notificationQuietHourEnd` INTEGER NOT NULL,
            `hideTaskDetailsOnLockScreen` INTEGER NOT NULL,
            `reduceMotionCelebration` INTEGER NOT NULL,
            `updatedAtMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          )
        """.trimIndent())
      }
    }

    fun getDatabase(context: Context): HeroDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          HeroDatabase::class.java,
          "hero_of_the_day_database"
        )
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3, MIGRATION_3_4)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

