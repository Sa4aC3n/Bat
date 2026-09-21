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
    ParentSecurityEntity::class
  ],
  version = 2,
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

    fun getDatabase(context: Context): HeroDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          HeroDatabase::class.java,
          "hero_of_the_day_database"
        )
          .addMigrations(MIGRATION_1_2)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}

