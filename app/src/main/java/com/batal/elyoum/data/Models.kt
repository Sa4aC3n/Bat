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
  val createdAtMillis: Long = System.currentTimeMillis(),
  val ageGroup: String = "ALL", // "ALL" or AgeGroup.code
  val category: String = "GENERAL"
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
  SKIPPED("SKIPPED", "تم تخطيها اليوم"),
  POSTPONED("POSTPONED", "مؤجلة لموعد لاحق");

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
  val updatedAtMillis: Long = System.currentTimeMillis(),
  val postponedToDate: String? = null,
  val originalOccurrenceId: String? = null
)

// --- Family Moments & Rewards ---

enum class RewardType(val code: String, val titleArabic: String, val iconName: String) {
  SHARED_TIME("SHARED_TIME", "وقت عائلي مشترك", "family_restroom"),
  ACTIVITY("ACTIVITY", "لعبة أو نشاط مميز", "sports_esports"),
  OUTING("OUTING", "نزهة أو زيارة لطيفة", "park"),
  MEAL_PREP("MEAL_PREP", "مشاركة في إعداد وجبة", "restaurant"),
  OPTIONAL_GIFT("OPTIONAL_GIFT", "هدية عينية بسيطة", "redeem");

  companion object {
    fun fromCode(code: String): RewardType = values().find { it.code == code } ?: SHARED_TIME
  }
}

enum class RewardGrantMode(val code: String, val titleArabic: String) {
  DIRECT_APPRECIATION("DIRECT_APPRECIATION", "تقدير مباشر ومفاجأة جميلة"),
  PRE_AGREED_GOAL("PRE_AGREED_GOAL", "هدف بسيط متفق عليه مسبقاً");

  companion object {
    fun fromCode(code: String): RewardGrantMode = values().find { it.code == code } ?: DIRECT_APPRECIATION
  }
}

enum class RewardStatus(val code: String, val titleArabic: String) {
  PLANNED("PLANNED", "مخططة"),
  AVAILABLE("AVAILABLE", "متاحة للاختيار"),
  REQUESTED("REQUESTED", "طلب الطفل تنفيذها"),
  FULFILLED("FULFILLED", "تمت بنجاح وتأكيد الوالدين"),
  CANCELLED("CANCELLED", "تم إلغاؤها بلطف");

  companion object {
    fun fromCode(code: String): RewardStatus = values().find { it.code == code } ?: PLANNED
  }
}

@Entity(
  tableName = "family_rewards",
  indices = [
    androidx.room.Index(value = ["childId"]),
    androidx.room.Index(value = ["status"])
  ]
)
data class FamilyRewardEntity(
  @PrimaryKey val id: String,
  val title: String,
  val description: String,
  val rewardType: String, // from RewardType.code
  val childId: String,
  val grantMode: String, // from RewardGrantMode.code
  val goalCriteria: String? = null,
  val targetDate: String? = null,
  val status: String, // from RewardStatus.code
  val cancellationReason: String? = null,
  val requestedAtMillis: Long? = null,
  val fulfilledAtMillis: Long? = null,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "family_reward_history",
  indices = [
    androidx.room.Index(value = ["rewardId"])
  ]
)
data class FamilyRewardHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val rewardId: String,
  val fromStatus: String,
  val toStatus: String,
  val note: String? = null,
  val timestampMillis: Long = System.currentTimeMillis()
)

// --- Editorial Content Review & Source Records ---

enum class MaterialType(val code: String, val labelArabic: String) {
  ORIGINAL_COMPOSITION("ORIGINAL_COMPOSITION", "صياغة أصلية ميسرة"),
  SUMMARY("SUMMARY", "تلخيص تربوي"),
  VERBATIM("VERBATIM", "نص منقول بدقة");

  companion object {
    fun fromCode(code: String): MaterialType = values().find { it.code == code } ?: ORIGINAL_COMPOSITION
  }
}

enum class ContentReviewStatus(val code: String, val labelArabic: String) {
  VERIFIED("VERIFIED", "تمت المراجعة والتحقق"),
  INCOMPLETE_DATA("INCOMPLETE_DATA", "بيانات المراجعة غير مكتملة"),
  PENDING_REVIEW("PENDING_REVIEW", "بانتظار المراجعة");

  companion object {
    fun fromCode(code: String): ContentReviewStatus = values().find { it.code == code } ?: INCOMPLETE_DATA
  }
}

@Entity(tableName = "content_review_records")
data class ContentReviewRecordEntity(
  @PrimaryKey val contentId: String,
  val contentVersion: Int = 1,
  val title: String,
  val text: String,
  val ageGroup: String, // AgeGroup.code or "ALL"
  val targetGoal: String,
  val materialType: String, // MaterialType.code
  val sourceReferenceTitle: String,
  val sourceAuthorOrEntity: String,
  val sourceUrl: String? = null,
  val sourceCitationLocation: String? = null,
  val reviewStatus: String, // ContentReviewStatus.code
  val reviewerName: String? = null,
  val reviewerSpecialty: String? = null,
  val reviewDate: String? = null,
  val approvalScope: String? = null,
  val reviewedVersion: Int? = null,
  val internalProofReference: String? = null,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val updatedAtMillis: Long = System.currentTimeMillis()
)

// --- App Settings & Customization ---

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
  val notificationsEnabled: Boolean = false,
  val notificationQuietHourStart: Int = 21,
  val notificationQuietHourEnd: Int = 8,
  val hideTaskDetailsOnLockScreen: Boolean = true,
  val reduceMotionCelebration: Boolean = false,
  val updatedAtMillis: Long = System.currentTimeMillis()
)

// --- Presets & Helpers ---

data class TaskPresetTemplate(
  val title: String,
  val description: String,
  val ageGroup: AgeGroup,
  val category: String,
  val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
  val requiresApproval: Boolean = false
)

object TaskPresetTemplates {
  val PRESETS = listOf(
    // 4-6
    TaskPresetTemplate("ترتيب ألعابي بعد اللعب", "أجمع ألعابي بلطف وأضعها في صندوق الألعاب.", AgeGroup.AGE_4_6, "المسؤولية والنظام"),
    TaskPresetTemplate("وضع الحذاء في مكانه", "أخلع حذائي عند الباب وأضعه في رف الأحذية المنظم.", AgeGroup.AGE_4_6, "النظام المنزلي"),
    TaskPresetTemplate("غسل اليدين بالماء والصابون", "أغسل يدي جيداً قبل الطعام وبعده وبعد اللعب.", AgeGroup.AGE_4_6, "النظافة والصحة"),
    TaskPresetTemplate("قول بسم الله والحمد لله", "أذكر اسم الله عند بدء الطعام وأحمده عند الانتهاء.", AgeGroup.AGE_4_6, "القيم والأذكار"),

    // 7-9
    TaskPresetTemplate("ترتيب سريري وغرفتي", "أرتب سريري صباحاً بعد الاستيقاظ وأحافظ على نظافة غرفتي.", AgeGroup.AGE_7_9, "المسؤولية الشخصية"),
    TaskPresetTemplate("المساعدة في إعداد السفرة", "أساعد ماما وبابا في وضع الأطباق أو رفعها بلطف.", AgeGroup.AGE_7_9, "التعاون الأسري"),
    TaskPresetTemplate("قراءة قصة أو صفحات مفيدة", "أقرأ لمدة ١٥ دقيقة في كتاب نافع أو قصة جميلة.", AgeGroup.AGE_7_9, "حب القراءة والتعلم"),
    TaskPresetTemplate("المحافظة على صلاتي في وقتها", "أستعد للصلاة عند سماع الأذان وأؤديها بهدوء.", AgeGroup.AGE_7_9, "العبادات والقيم", requiresApproval = true),
    TaskPresetTemplate("سؤال الوالدين ومساعدتهما", "أسأل ماما وبابا إن كانا بحاجة لمساعدة وأسمع كلامهما.", AgeGroup.AGE_7_9, "بر الوالدين"),

    // 10-12
    TaskPresetTemplate("تنظيم مكتبي وواجباتي المدرسية", "أنهي واجباتي أولاً بأول وأرتب مكتبي وحقيبتي للغد.", AgeGroup.AGE_10_12, "الاجتهاد الدراسي"),
    TaskPresetTemplate("مهمة منزلية مستقلة", "أتحمل مسؤولية مهمة كاملة (رمي النفايات، سقي النباتات، أو مساعدة أخي الصغير).", AgeGroup.AGE_10_12, "المسؤولية الأسرية"),
    TaskPresetTemplate("ممارسة نشاط بدني أو رياضة", "نصف ساعة من الحركة أو التمارين أو المشي لصحة قوية.", AgeGroup.AGE_10_12, "الصحة والنشاط"),
    TaskPresetTemplate("تدبر آيات من القرآن الكريم", "تلاوة آيات بتأنٍ ومحاولة فهم معناها والعمل بها.", AgeGroup.AGE_10_12, "القيم الإيمانية")
  )
}

object FamilyRewardPresets {
  data class RewardPreset(val title: String, val description: String, val type: RewardType)

  val PRESETS = listOf(
    RewardPreset("قراءة قصة مشوقة مع أحد الوالدين", "جلسة هادئة وممتعة نقرأ فيها معاً قصة يختارها الطفل.", RewardType.SHARED_TIME),
    RewardPreset("لعبة عائلية مسلية معاً", "وقت مرح يجمع العائلة للعب لعبة ألواح أو تحدٍ ممتع.", RewardType.ACTIVITY),
    RewardPreset("اختيار النشاط المشترك لليوم", "يختار الطفل النشاط الأسري المسائي المفضل له.", RewardType.ACTIVITY),
    RewardPreset("نزهة أو زيارة لطيفة متفق عليها", "خروجة جميلة للحديقة أو زيارة للأقارب والأصدقاء.", RewardType.OUTING),
    RewardPreset("مشاركة في إعداد وجبة أو حلوى يحبها الطفل", "ندخل المطبخ معاً لنصنع شيئاً لذيذاً بروح التعاون.", RewardType.MEAL_PREP),
    RewardPreset("هدية بسيطة يحددها الوالدان", "مفاجأة تشجيعية عينية بسيطة يعبّر بها الوالدان عن تقديرهما.", RewardType.OPTIONAL_GIFT)
  )
}

// --- Parent Security & PIN Gate ---

@Entity(tableName = "parent_security")
data class ParentSecurityEntity(
  @PrimaryKey val id: Int = 1,
  val pinSalt: String,
  val pinHash: String,
  val failedAttempts: Int = 0,
  val lockoutUntilMillis: Long = 0L,
  val isConfigured: Boolean = false,
  val algoVersion: Int = 1,
  val iterations: Int = 10000,
  val algorithm: String = "PBKDF2WithHmacSHA256",
  val updatedAtMillis: Long = System.currentTimeMillis()
)

