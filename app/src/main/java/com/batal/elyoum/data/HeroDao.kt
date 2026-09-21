package com.batal.elyoum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroDao {
  // Honored Heroes (personal nominations)
  @Query("SELECT * FROM honored_heroes ORDER BY dateMillis DESC")
  fun getAllHonoredHeroes(): Flow<List<HonoredHeroEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHonoredHero(hero: HonoredHeroEntity): Long

  @Query("DELETE FROM honored_heroes WHERE id = :id")
  suspend fun deleteHonoredHero(id: Long)

  // Daily deeds progress
  @Query("SELECT * FROM daily_deeds_progress WHERE dateStr = :dateStr")
  fun getDeedsForDate(dateStr: String): Flow<List<DailyDeedProgressEntity>>

  @Query("SELECT COUNT(DISTINCT dateStr) FROM daily_deeds_progress WHERE isCompleted = 1")
  fun getCompletedDaysCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDeedProgress(progress: DailyDeedProgressEntity)

  @Query("SELECT * FROM daily_deeds_progress WHERE deedKey = :deedKey LIMIT 1")
  suspend fun getDeedProgress(deedKey: String): DailyDeedProgressEntity?

  @Query("DELETE FROM daily_deeds_progress WHERE deedKey = :deedKey")
  suspend fun deleteDeedProgress(deedKey: String)

  // Favorites
  @Query("SELECT * FROM favorite_heroes")
  fun getAllFavorites(): Flow<List<FavoriteHeroEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFavorite(fav: FavoriteHeroEntity)

  @Query("DELETE FROM favorite_heroes WHERE heroId = :heroId")
  suspend fun deleteFavorite(heroId: String)

  // --- Child Profiles ---
  @Query("SELECT * FROM child_profiles WHERE isArchived = 0 ORDER BY createdAtMillis ASC")
  fun getActiveChildren(): Flow<List<ChildProfileEntity>>

  @Query("SELECT * FROM child_profiles ORDER BY isArchived ASC, createdAtMillis ASC")
  fun getAllChildren(): Flow<List<ChildProfileEntity>>

  @Query("SELECT * FROM child_profiles WHERE id = :id")
  suspend fun getChildById(id: String): ChildProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChild(child: ChildProfileEntity)

  @Query("UPDATE child_profiles SET isArchived = 1 WHERE id = :childId")
  suspend fun archiveChild(childId: String)

  @Query("UPDATE child_profiles SET alias = :alias, ageGroup = :ageGroup, avatarId = :avatarId WHERE id = :id")
  suspend fun updateChildProfile(id: String, alias: String, ageGroup: String, avatarId: String)

  // --- Parent Tasks ---
  @Query("SELECT * FROM parent_tasks WHERE isArchived = 0 ORDER BY createdAtMillis DESC")
  fun getActiveTasks(): Flow<List<ParentTaskEntity>>

  @Query("SELECT * FROM parent_tasks ORDER BY isArchived ASC, createdAtMillis DESC")
  fun getAllTasks(): Flow<List<ParentTaskEntity>>

  @Query("SELECT * FROM parent_tasks WHERE id = :taskId")
  suspend fun getTaskById(taskId: String): ParentTaskEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: ParentTaskEntity)

  @Query("UPDATE parent_tasks SET isArchived = 1 WHERE id = :taskId")
  suspend fun archiveTask(taskId: String)

  @Query("UPDATE parent_tasks SET title = :title, description = :description, requiresApproval = :requiresApproval, recurrenceType = :recurrenceType, targetDaysOfWeek = :targetDaysOfWeek, startDate = :startDate WHERE id = :taskId")
  suspend fun updateTask(taskId: String, title: String, description: String, requiresApproval: Boolean, recurrenceType: String, targetDaysOfWeek: String, startDate: String)

  @Transaction
  suspend fun insertTaskWithAssignments(task: ParentTaskEntity, assignments: List<TaskAssignmentEntity>) {
    insertTask(task)
    deleteAssignmentsForTask(task.id)
    if (assignments.isNotEmpty()) {
      insertAssignments(assignments)
    }
  }

  @Transaction
  suspend fun updateTaskWithAssignments(task: ParentTaskEntity, assignments: List<TaskAssignmentEntity>) {
    updateTask(
      taskId = task.id,
      title = task.title,
      description = task.description,
      requiresApproval = task.requiresApproval,
      recurrenceType = task.recurrenceType,
      targetDaysOfWeek = task.targetDaysOfWeek,
      startDate = task.startDate
    )
    deleteAssignmentsForTask(task.id)
    if (assignments.isNotEmpty()) {
      insertAssignments(assignments)
    }
  }

  // --- Task Assignments ---
  @Query("SELECT * FROM task_assignments WHERE taskId = :taskId")
  suspend fun getAssignmentsForTask(taskId: String): List<TaskAssignmentEntity>

  @Query("SELECT * FROM task_assignments WHERE childId = :childId")
  suspend fun getAssignmentsForChild(childId: String): List<TaskAssignmentEntity>

  @Query("SELECT taskId FROM task_assignments WHERE childId = :childId")
  fun getAssignedTaskIdsForChild(childId: String): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAssignments(assignments: List<TaskAssignmentEntity>)

  @Query("DELETE FROM task_assignments WHERE taskId = :taskId")
  suspend fun deleteAssignmentsForTask(taskId: String)

  // --- Task Occurrences ---
  @Query("SELECT * FROM task_occurrences WHERE childId = :childId AND dateStr = :dateStr ORDER BY updatedAtMillis DESC")
  fun getOccurrencesForChildAndDate(childId: String, dateStr: String): Flow<List<TaskOccurrenceEntity>>

  @Query("SELECT * FROM task_occurrences WHERE status = 'PENDING_APPROVAL' ORDER BY updatedAtMillis DESC")
  fun getPendingApprovalOccurrences(): Flow<List<TaskOccurrenceEntity>>

  @Query("SELECT * FROM task_occurrences WHERE id = :occurrenceId LIMIT 1")
  suspend fun getOccurrenceById(occurrenceId: String): TaskOccurrenceEntity?

  @Query("SELECT * FROM parent_tasks WHERE id = :taskId LIMIT 1")
  suspend fun getParentTaskById(taskId: String): ParentTaskEntity?

  @Query("SELECT * FROM task_occurrences WHERE taskId = :taskId AND childId = :childId AND dateStr = :dateStr LIMIT 1")
  suspend fun getOccurrence(taskId: String, childId: String, dateStr: String): TaskOccurrenceEntity?

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertOccurrenceIfNotExists(occurrence: TaskOccurrenceEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun updateOccurrence(occurrence: TaskOccurrenceEntity)

  @Query("UPDATE task_occurrences SET status = :status, completedAtMillis = :completedAtMillis, updatedAtMillis = :updatedAtMillis WHERE id = :occurrenceId")
  suspend fun updateOccurrenceStatus(occurrenceId: String, status: String, completedAtMillis: Long?, updatedAtMillis: Long)

  @Query("UPDATE task_occurrences SET status = :status, parentFeedbackNote = :note, updatedAtMillis = :updatedAtMillis WHERE id = :occurrenceId")
  suspend fun reviewOccurrence(occurrenceId: String, status: String, note: String?, updatedAtMillis: Long)

  @Transaction
  suspend fun submitOccurrenceAtomically(
    occurrenceId: String,
    childId: String,
    now: Long
  ): TaskOccurrenceEntity {
    val occ = getOccurrenceById(occurrenceId)
      ?: throw IllegalArgumentException("سجل المهمة غير موجود")
    if (occ.childId != childId) {
      throw IllegalStateException("المهمة لا تنتمي للطفل المحدد")
    }
    if (occ.status == TaskOccurrenceStatus.COMPLETED.code) {
      throw IllegalStateException("لا يمكن تعديل مهمة مكتملة بالفعل")
    }
    if (occ.status == TaskOccurrenceStatus.PENDING_APPROVAL.code) {
      throw IllegalStateException("المهمة قيد الانتظار لموافقة ولي الأمر بالفعل")
    }

    // Rely exclusively on requiresApprovalSnapshot stored in the occurrence record
    val requiresApproval = occ.requiresApprovalSnapshot

    val newStatus = if (requiresApproval) {
      TaskOccurrenceStatus.PENDING_APPROVAL.code
    } else {
      TaskOccurrenceStatus.COMPLETED.code
    }

    val updated = occ.copy(
      status = newStatus,
      completedAtMillis = if (newStatus == TaskOccurrenceStatus.COMPLETED.code) now else occ.completedAtMillis,
      updatedAtMillis = now
    )
    updateOccurrence(updated)
    return updated
  }

  @Transaction
  suspend fun cancelPendingApprovalAtomically(
    occurrenceId: String,
    childId: String,
    now: Long
  ): TaskOccurrenceEntity {
    val occ = getOccurrenceById(occurrenceId)
      ?: throw IllegalArgumentException("سجل المهمة غير موجود")
    if (occ.childId != childId) {
      throw IllegalStateException("المهمة لا تنتمي للطفل المحدد")
    }
    if (occ.status == TaskOccurrenceStatus.COMPLETED.code) {
      throw IllegalStateException("لا يمكن تعديل مهمة مكتملة بالفعل")
    }
    if (occ.status != TaskOccurrenceStatus.PENDING_APPROVAL.code) {
      throw IllegalStateException("المهمة ليست قيد الانتظار لموافقة ولي الأمر")
    }

    val updated = occ.copy(
      status = TaskOccurrenceStatus.NOT_STARTED.code,
      completedAtMillis = null,
      updatedAtMillis = now
    )
    updateOccurrence(updated)
    return updated
  }

  @Transaction
  suspend fun skipOccurrenceAtomically(
    occurrenceId: String,
    childId: String,
    now: Long
  ): TaskOccurrenceEntity {
    val occ = getOccurrenceById(occurrenceId)
      ?: throw IllegalArgumentException("سجل المهمة غير موجود")
    if (occ.childId != childId) {
      throw IllegalStateException("المهمة لا تنتمي للطفل المحدد")
    }
    if (occ.status == TaskOccurrenceStatus.COMPLETED.code) {
      throw IllegalStateException("لا يمكن تخطي مهمة مكتملة بالفعل")
    }
    if (occ.status == TaskOccurrenceStatus.SKIPPED.code) {
      return occ
    }

    val updated = occ.copy(
      status = TaskOccurrenceStatus.SKIPPED.code,
      completedAtMillis = null,
      updatedAtMillis = now
    )
    updateOccurrence(updated)
    return updated
  }

  @Transaction
  suspend fun postponeOccurrenceAtomically(
    occurrenceId: String,
    childId: String,
    targetDateStr: String,
    now: Long
  ): TaskOccurrenceEntity {
    val occ = getOccurrenceById(occurrenceId)
      ?: throw IllegalArgumentException("سجل المهمة غير موجود")
    if (occ.childId != childId) {
      throw IllegalStateException("المهمة لا تنتمي للطفل المحدد")
    }
    if (occ.status == TaskOccurrenceStatus.COMPLETED.code) {
      throw IllegalStateException("لا يمكن تأجيل مهمة مكتملة بالفعل")
    }
    if (occ.status == TaskOccurrenceStatus.PENDING_APPROVAL.code) {
      throw IllegalStateException("المهمة بانتظار الاعتماد؛ يجب البت فيها أولاً")
    }
    if (targetDateStr <= occ.dateStr) {
      throw IllegalArgumentException("تاريخ التأجيل يجب أن يكون في المستقبل")
    }

    // Check if occurrence already exists on targetDateStr for this task and child
    val existingOnTarget = getOccurrence(occ.taskId, childId, targetDateStr)
    if (existingOnTarget == null) {
      val newOccurrence = TaskOccurrenceEntity(
        id = java.util.UUID.randomUUID().toString(),
        taskId = occ.taskId,
        childId = childId,
        dateStr = targetDateStr,
        status = TaskOccurrenceStatus.NOT_STARTED.code,
        snapshotTitle = occ.snapshotTitle,
        snapshotDescription = occ.snapshotDescription,
        requiresApprovalSnapshot = occ.requiresApprovalSnapshot,
        completedAtMillis = null,
        parentFeedbackNote = null,
        updatedAtMillis = now,
        postponedToDate = null,
        originalOccurrenceId = occ.id
      )
      insertOccurrenceIfNotExists(newOccurrence)
    }

    val updatedOriginal = occ.copy(
      status = TaskOccurrenceStatus.POSTPONED.code,
      postponedToDate = targetDateStr,
      updatedAtMillis = now
    )
    updateOccurrence(updatedOriginal)
    return updatedOriginal
  }

  @Query("SELECT * FROM task_occurrences WHERE childId = :childId AND dateStr BETWEEN :startDate AND :endDate ORDER BY dateStr ASC")
  fun getOccurrencesForChildBetweenDates(childId: String, startDate: String, endDate: String): Flow<List<TaskOccurrenceEntity>>

  @Query("SELECT * FROM task_occurrences WHERE childId = :childId AND dateStr BETWEEN :startDate AND :endDate ORDER BY dateStr ASC")
  suspend fun getOccurrencesForChildBetweenDatesSync(childId: String, startDate: String, endDate: String): List<TaskOccurrenceEntity>

  @Transaction
  suspend fun submitTaskByTaskIdAtomically(
    taskId: String,
    childId: String,
    dateStr: String,
    now: Long
  ): TaskOccurrenceEntity {
    val occ = getOccurrence(taskId, childId, dateStr)
      ?: throw IllegalArgumentException("لا يوجد سجل لهذه المهمة لليوم المحدد")
    return submitOccurrenceAtomically(occ.id, childId, now)
  }

  // --- Family Rewards & Moments ---

  @Query("SELECT * FROM family_rewards WHERE childId = :childId ORDER BY createdAtMillis DESC")
  fun getRewardsForChildFlow(childId: String): Flow<List<FamilyRewardEntity>>

  @Query("SELECT * FROM family_rewards WHERE childId = :childId AND status IN ('AVAILABLE', 'REQUESTED') ORDER BY createdAtMillis DESC")
  fun getActiveRewardsForChildFlow(childId: String): Flow<List<FamilyRewardEntity>>

  @Query("SELECT * FROM family_rewards WHERE id = :rewardId LIMIT 1")
  suspend fun getRewardById(rewardId: String): FamilyRewardEntity?

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertReward(reward: FamilyRewardEntity)

  @Update
  suspend fun updateReward(reward: FamilyRewardEntity)

  @Query("DELETE FROM family_rewards WHERE id = :rewardId")
  suspend fun deleteReward(rewardId: String)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRewardHistory(history: FamilyRewardHistoryEntity)

  @Query("SELECT * FROM family_reward_history WHERE rewardId = :rewardId ORDER BY timestampMillis ASC")
  suspend fun getHistoryForReward(rewardId: String): List<FamilyRewardHistoryEntity>

  @Transaction
  suspend fun makeRewardAvailableAtomically(rewardId: String, now: Long): FamilyRewardEntity {
    val reward = getRewardById(rewardId) ?: throw IllegalArgumentException("المكافأة غير موجودة")
    val updated = reward.copy(
      status = RewardStatus.AVAILABLE.code,
      updatedAtMillis = now
    )
    updateReward(updated)
    insertRewardHistory(
      FamilyRewardHistoryEntity(
        rewardId = rewardId,
        fromStatus = reward.status,
        toStatus = RewardStatus.AVAILABLE.code,
        note = "تمت إتاحة المكافأة للطفل للاختيار",
        timestampMillis = now
      )
    )
    return updated
  }

  @Transaction
  suspend fun requestRewardAtomically(rewardId: String, childId: String, now: Long): FamilyRewardEntity {
    val reward = getRewardById(rewardId) ?: throw IllegalArgumentException("المكافأة غير موجودة")
    if (reward.childId != childId) {
      throw IllegalStateException("المكافأة غير مخصصة لهذا الطفل")
    }
    if (reward.status == RewardStatus.REQUESTED.code) {
      // Idempotent for rapid clicks
      return reward
    }
    if (reward.status != RewardStatus.AVAILABLE.code) {
      throw IllegalStateException("المكافأة غير متاحة للطلب حالياً (حالتها: ${reward.status})")
    }
    val updated = reward.copy(
      status = RewardStatus.REQUESTED.code,
      requestedAtMillis = now,
      updatedAtMillis = now
    )
    updateReward(updated)
    insertRewardHistory(
      FamilyRewardHistoryEntity(
        rewardId = rewardId,
        fromStatus = reward.status,
        toStatus = RewardStatus.REQUESTED.code,
        note = "طلب الطفل تنفيذ هذه اللحظة الجميلة",
        timestampMillis = now
      )
    )
    return updated
  }

  @Transaction
  suspend fun fulfillRewardAtomically(rewardId: String, note: String?, now: Long): FamilyRewardEntity {
    val reward = getRewardById(rewardId) ?: throw IllegalArgumentException("المكافأة غير موجودة")
    if (reward.status == RewardStatus.FULFILLED.code) {
      return reward
    }
    val updated = reward.copy(
      status = RewardStatus.FULFILLED.code,
      fulfilledAtMillis = now,
      updatedAtMillis = now
    )
    updateReward(updated)
    insertRewardHistory(
      FamilyRewardHistoryEntity(
        rewardId = rewardId,
        fromStatus = reward.status,
        toStatus = RewardStatus.FULFILLED.code,
        note = note?.trim() ?: "تمت مشاركة اللحظة بنجاح وتأكيد الوالدين",
        timestampMillis = now
      )
    )
    return updated
  }

  @Transaction
  suspend fun cancelRewardAtomically(rewardId: String, reason: String, now: Long): FamilyRewardEntity {
    val reward = getRewardById(rewardId) ?: throw IllegalArgumentException("المكافأة غير موجودة")
    val updated = reward.copy(
      status = RewardStatus.CANCELLED.code,
      cancellationReason = reason.trim(),
      updatedAtMillis = now
    )
    updateReward(updated)
    insertRewardHistory(
      FamilyRewardHistoryEntity(
        rewardId = rewardId,
        fromStatus = reward.status,
        toStatus = RewardStatus.CANCELLED.code,
        note = "تم الإلغاء بلطف: ${reason.trim()}",
        timestampMillis = now
      )
    )
    return updated
  }

  @Query("SELECT * FROM family_rewards WHERE childId = :childId AND status = 'FULFILLED' AND fulfilledAtMillis BETWEEN :startMillis AND :endMillis")
  suspend fun getFulfilledRewardsForChildBetween(childId: String, startMillis: Long, endMillis: Long): List<FamilyRewardEntity>

  // --- Editorial Content Review Records ---

  @Query("SELECT * FROM content_review_records ORDER BY contentId ASC")
  fun getAllContentReviewRecordsFlow(): Flow<List<ContentReviewRecordEntity>>

  @Query("SELECT * FROM content_review_records WHERE contentId = :contentId LIMIT 1")
  fun getContentReviewRecordFlow(contentId: String): Flow<ContentReviewRecordEntity?>

  @Query("SELECT * FROM content_review_records WHERE contentId = :contentId LIMIT 1")
  suspend fun getContentReviewRecord(contentId: String): ContentReviewRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContentReviewRecord(record: ContentReviewRecordEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContentReviewRecords(records: List<ContentReviewRecordEntity>)

  @Query("SELECT COUNT(*) FROM content_review_records")
  suspend fun countContentReviewRecords(): Int

  // --- App Settings ---

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  fun getAppSettingsFlow(): Flow<AppSettingsEntity?>

  @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
  suspend fun getAppSettingsSync(): AppSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateAppSettings(settings: AppSettingsEntity)

  // --- Cascade Data Deletion ---

  @Transaction
  suspend fun deleteChildPermanentlyAtomically(childId: String) {
    // 1. Delete reward history for child's rewards
    deleteRewardHistoryForChild(childId)
    // 2. Delete rewards for child
    deleteRewardsForChild(childId)
    // 3. Delete task occurrences for child
    deleteOccurrencesForChild(childId)
    // 4. Delete task assignments for child
    deleteAssignmentsForChild(childId)
    // 5. Delete child profile
    deleteChildProfile(childId)
  }

  @Query("DELETE FROM family_reward_history WHERE rewardId IN (SELECT id FROM family_rewards WHERE childId = :childId)")
  suspend fun deleteRewardHistoryForChild(childId: String)

  @Query("DELETE FROM family_rewards WHERE childId = :childId")
  suspend fun deleteRewardsForChild(childId: String)

  @Query("DELETE FROM task_occurrences WHERE childId = :childId")
  suspend fun deleteOccurrencesForChild(childId: String)

  @Query("DELETE FROM task_assignments WHERE childId = :childId")
  suspend fun deleteAssignmentsForChild(childId: String)

  @Query("DELETE FROM child_profiles WHERE id = :childId")
  suspend fun deleteChildProfile(childId: String)

  @Transaction
  suspend fun deleteAllFamilyDataAtomically() {
    clearAllRewardHistories()
    clearAllRewards()
    clearAllOccurrences()
    clearAllAssignments()
    clearAllTasks()
    clearAllChildProfiles()
  }

  @Query("DELETE FROM family_reward_history")
  suspend fun clearAllRewardHistories()

  @Query("DELETE FROM family_rewards")
  suspend fun clearAllRewards()

  @Query("DELETE FROM task_occurrences")
  suspend fun clearAllOccurrences()

  @Query("DELETE FROM task_assignments")
  suspend fun clearAllAssignments()

  @Query("DELETE FROM parent_tasks")
  suspend fun clearAllTasks()

  @Query("DELETE FROM child_profiles")
  suspend fun clearAllChildProfiles()

  @Query("SELECT * FROM child_profiles ORDER BY createdAtMillis ASC")
  suspend fun getAllChildrenSync(): List<ChildProfileEntity>

  @Query("SELECT * FROM parent_tasks ORDER BY createdAtMillis ASC")
  suspend fun getAllTasksSync(): List<ParentTaskEntity>

  @Query("SELECT * FROM task_occurrences ORDER BY dateStr ASC")
  suspend fun getAllOccurrencesSync(): List<TaskOccurrenceEntity>

  @Query("SELECT * FROM family_rewards ORDER BY createdAtMillis ASC")
  suspend fun getAllRewardsSync(): List<FamilyRewardEntity>

  // --- Parent Security ---
  @Query("SELECT * FROM parent_security WHERE id = 1 LIMIT 1")
  fun getParentSecurityFlow(): Flow<ParentSecurityEntity?>

  @Query("SELECT * FROM parent_security WHERE id = 1 LIMIT 1")
  suspend fun getParentSecurity(): ParentSecurityEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateParentSecurity(security: ParentSecurityEntity)
}
