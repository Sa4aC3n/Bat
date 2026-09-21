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

    val parentTask = getParentTaskById(occ.taskId)
    val requiresApproval = parentTask?.requiresApproval ?: occ.requiresApprovalSnapshot

    val newStatus = if (requiresApproval) {
      TaskOccurrenceStatus.PENDING_APPROVAL.code
    } else {
      TaskOccurrenceStatus.COMPLETED.code
    }

    val updated = occ.copy(
      status = newStatus,
      requiresApprovalSnapshot = requiresApproval,
      completedAtMillis = if (newStatus == TaskOccurrenceStatus.COMPLETED.code) now else occ.completedAtMillis,
      updatedAtMillis = now
    )
    updateOccurrence(updated)
    return updated
  }

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

  // --- Parent Security ---
  @Query("SELECT * FROM parent_security WHERE id = 1 LIMIT 1")
  fun getParentSecurityFlow(): Flow<ParentSecurityEntity?>

  @Query("SELECT * FROM parent_security WHERE id = 1 LIMIT 1")
  suspend fun getParentSecurity(): ParentSecurityEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateParentSecurity(security: ParentSecurityEntity)
}
