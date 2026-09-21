package com.batal.elyoum.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HeroCategory(val titleArabic: String, val iconName: String) {
  SCIENCE("علوم وابتكار", "science"),
  HUMANITY("إنسانية وعطاء", "volunteer_activism"),
  COURAGE("شجاعة وريادة", "military_tech"),
  WISDOM("فكر وحكمة", "psychology"),
  EVERYDAY("أبطال واقعنا", "verified_user")
}

data class Hero(
  val id: String,
  val name: String,
  val title: String,
  val category: HeroCategory,
  val era: String,
  val quote: String,
  val story: String,
  val virtues: List<String>,
  val dailyHeroicLesson: String,
  val iconName: String
)

@Entity(tableName = "honored_heroes")
data class HonoredHeroEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val relation: String,
  val reason: String,
  val badgeName: String,
  val dateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_deeds_progress")
data class DailyDeedProgressEntity(
  @PrimaryKey val deedKey: String, // format: "yyyy-MM-dd_deedId"
  val dateStr: String,
  val deedId: String,
  val isCompleted: Boolean,
  val completedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_heroes")
data class FavoriteHeroEntity(
  @PrimaryKey val heroId: String,
  val addedAtMillis: Long = System.currentTimeMillis()
)

data class DailyDeed(
  val id: String,
  val title: String,
  val description: String,
  val points: Int,
  val category: String
)

// --- Child Profiles ---

enum class AgeGroup(val code: String, val label: String, val ageRange: String) {
  AGE_4_6("AGE_4_6", "الطفولة المبكرة (٤ - ٦ سنوات)", "4 - 6"),
  AGE_7_9("AGE_7_9", "المرحلة المتوسطة (٧ - ٩ سنوات)", "7 - 9"),
  AGE_10_12("AGE_10_12", "الناشئة الصغار (١٠ - ١٢ سنة)", "10 - 12");

  companion object {
    fun fromCode(code: String): AgeGroup = values().find { it.code == code } ?: AGE_7_9
  }
}

data class AvatarPreset(
  val id: String,
  val title: String,
  val iconName: String
)

object AvatarPresets {
  val PRESETS = listOf(
    AvatarPreset("avatar_lion", "الأسد الشجاع", "pets"),
    AvatarPreset("avatar_falcon", "الصقر المحلق", "flight"),
    AvatarPreset("avatar_horse", "الخيل الأصيل", "speed"),
    AvatarPreset("avatar_star", "النجم المضيء", "star"),
    AvatarPreset("avatar_sun", "شمس الصباح", "wb_sunny"),
    AvatarPreset("avatar_dove", "حمامة السلام", "eco")
  )
  fun getById(id: String): AvatarPreset = PRESETS.find { it.id == id } ?: PRESETS[0]
}

@Entity(tableName = "child_profiles")
data class ChildProfileEntity(
  @PrimaryKey val id: String,
  val alias: String,
  val ageGroup: String, // from AgeGroup.code
  val avatarId: String,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val isArchived: Boolean = false
)

// --- Parent Tasks & Scheduling ---

enum class RecurrenceType(val code: String, val label: String) {
  ONCE("ONCE", "مرة واحدة فقط"),
  DAILY("DAILY", "يوميًا"),
  CUSTOM_DAYS("CUSTOM_DAYS", "أيام محددة من الأسبوع");

  companion object {
    fun fromCode(code: String): RecurrenceType = values().find { it.code == code } ?: DAILY
  }
}

@Entity(tableName = "parent_tasks")
data class ParentTaskEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  val requiresApproval: Boolean,
  val recurrenceType: String, // from RecurrenceType.code
  val targetDaysOfWeek: String = "", // comma-separated Calendar.DAY_OF_WEEK e.g. "1,2,3,4,5,6,7"
  val startDate: String, // "yyyy-MM-dd"
  val isArchived: Boolean = false,
  val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "task_assignments",
  indices = [
    androidx.room.Index(value = ["taskId", "childId"], unique = true),
    androidx.room.Index(value = ["childId"])
  ]
)
data class TaskAssignmentEntity(
  @PrimaryKey val id: String,
  val taskId: String,
  val childId: String,
  val createdAtMillis: Long = System.currentTimeMillis()
)

// --- Task Occurrence & Progress Lifecycle ---

enum class TaskOccurrenceStatus(val code: String, val label: String) {
  NOT_STARTED("NOT_STARTED", "لم تبدأ"),
  PENDING_APPROVAL("PENDING_APPROVAL", "بانتظار تأكيد ولي الأمر"),
  COMPLETED("COMPLETED", "مكتملة"),
  SKIPPED("SKIPPED", "تم تخطيها اليوم");

  companion object {
    fun fromCode(code: String): TaskOccurrenceStatus = values().find { it.code == code } ?: NOT_STARTED
  }
}

@Entity(
  tableName = "task_occurrences",
  indices = [
    androidx.room.Index(value = ["taskId", "childId", "dateStr"], unique = true),
    androidx.room.Index(value = ["childId", "dateStr"])
  ]
)
data class TaskOccurrenceEntity(
  @PrimaryKey val id: String,
  val taskId: String,
  val childId: String,
  val dateStr: String, // "yyyy-MM-dd"
  val status: String, // from TaskOccurrenceStatus.code
  val snapshotTitle: String,
  val snapshotDescription: String,
  val requiresApprovalSnapshot: Boolean,
  val completedAtMillis: Long? = null,
  val parentFeedbackNote: String? = null,
  val updatedAtMillis: Long = System.currentTimeMillis()
)

// --- Parent Security & PIN Gate ---

@Entity(tableName = "parent_security")
data class ParentSecurityEntity(
  @PrimaryKey val id: Int = 1,
  val pinSalt: String,
  val pinHash: String,
  val failedAttempts: Int = 0,
  val lockoutUntilMillis: Long = 0L,
  val isConfigured: Boolean = false,
  val updatedAtMillis: Long = System.currentTimeMillis()
)

