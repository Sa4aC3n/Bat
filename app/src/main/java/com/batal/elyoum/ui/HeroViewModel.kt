package com.batal.elyoum.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.batal.elyoum.data.AgeGroup
import com.batal.elyoum.data.AppSettingsEntity
import com.batal.elyoum.data.ChildProfileEntity
import com.batal.elyoum.data.ContentReviewRecordEntity
import com.batal.elyoum.data.DailyDeed
import com.batal.elyoum.data.DefaultTimeProvider
import com.batal.elyoum.data.FamilyRewardEntity
import com.batal.elyoum.data.FamilyRewardHistoryEntity
import com.batal.elyoum.data.Hero
import com.batal.elyoum.data.HeroCategory
import com.batal.elyoum.data.HeroRepository
import com.batal.elyoum.data.HonoredHeroEntity
import com.batal.elyoum.data.ParentSecurityEntity
import com.batal.elyoum.data.ParentTaskEntity
import com.batal.elyoum.data.RecurrenceType
import com.batal.elyoum.data.RewardGrantMode
import com.batal.elyoum.data.RewardStatus
import com.batal.elyoum.data.RewardType
import com.batal.elyoum.data.TaskOccurrenceEntity
import com.batal.elyoum.data.TaskOccurrenceStatus
import com.batal.elyoum.data.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class DeedWithStatus(
  val deed: DailyDeed,
  val isCompleted: Boolean
)

data class QuizQuestion(
  val question: String,
  val options: List<QuizOption>
)

data class QuizOption(
  val text: String,
  val categoryMatch: HeroCategory
)

data class QuizResult(
  val heroCategory: HeroCategory,
  val matchedHero: Hero,
  val archetypeTitle: String,
  val description: String
)

class HeroViewModel(
  application: Application,
  customRepository: HeroRepository? = null,
  private val timeProvider: TimeProvider = DefaultTimeProvider()
) : AndroidViewModel(application) {
  val repository: HeroRepository = customRepository ?: HeroRepository(application, timeProvider = timeProvider)

  val todayHero: Hero = repository.getTodayHero()

  private val _selectedHero = MutableStateFlow<Hero>(todayHero)
  val selectedHero: StateFlow<Hero> = _selectedHero.asStateFlow()

  val allHeroes: List<Hero> = HeroRepository.HEROES

  // Honored heroes list
  val honoredHeroes: StateFlow<List<HonoredHeroEntity>> = repository.allHonoredHeroes.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  // Favorite hero IDs
  val favoriteHeroIds: StateFlow<Set<String>> = repository.favorites.combine(MutableStateFlow(Unit)) { favs, _ ->
    favs.map { it.heroId }.toSet()
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptySet()
  )

  // Daily deeds combined with completion status
  val dailyDeeds: StateFlow<List<DeedWithStatus>> = repository.getDeedsForToday().combine(
    MutableStateFlow(HeroRepository.DEFAULT_DEEDS)
  ) { completedEntities, defaultDeeds ->
    val completedIds = completedEntities.filter { it.isCompleted }.map { it.deedId }.toSet()
    defaultDeeds.map { deed ->
      DeedWithStatus(
        deed = deed,
        isCompleted = completedIds.contains(deed.id)
      )
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = HeroRepository.DEFAULT_DEEDS.map { DeedWithStatus(it, false) }
  )

  val totalDaysActive: StateFlow<Int> = repository.totalDaysActive.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = 0
  )

  // Quiz state
  val quizQuestions = listOf(
    QuizQuestion(
      question = "ما هو الدافع الأكبر الذي يحركك في الصباح؟",
      options = listOf(
        QuizOption("اكتشاف فكرة جديدة وفهم أسرار العالم", HeroCategory.SCIENCE),
        QuizOption("رسم بسمة على وجه متألم ومساعدة محتاج", HeroCategory.HUMANITY),
        QuizOption("خوض تحدٍ صعب والدفاع عن الحق", HeroCategory.COURAGE),
        QuizOption("نشر المعرفة وبناء أثر يدوم للأجيال", HeroCategory.WISDOM)
      )
    ),
    QuizQuestion(
      question = "لو أتيحت لك قوة استثنائية ليوم واحد، ماذا تختار؟",
      options = listOf(
        QuizOption("علاج أي مرض مستعصٍ وشفاء الآلام", HeroCategory.EVERYDAY),
        QuizOption("حل أعقد معادلات الطاقة والكون", HeroCategory.SCIENCE),
        QuizOption("الشجاعة المطلقة لإنقاذ أي إنسان في خطر", HeroCategory.COURAGE),
        QuizOption("تأسيس مدارس وجامعات في كل بقعة محرومة", HeroCategory.WISDOM)
      )
    ),
    QuizQuestion(
      question = "ما هي الصفة التي تفتخر بوجودها في شخصيتك؟",
      options = listOf(
        QuizOption("الصبر والرحمة والتفاني في العطاء", HeroCategory.HUMANITY),
        QuizOption("حب البحث والاستكشاف ورفض التقليد الأعمى", HeroCategory.SCIENCE),
        QuizOption("الجسارة في قول الحق والثبات عند الشدائد", HeroCategory.COURAGE),
        QuizOption("الحكمة وبعد النظر والعدالة", HeroCategory.WISDOM)
      )
    )
  )

  private val _currentQuizIndex = MutableStateFlow(0)
  val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

  private val _quizAnswers = MutableStateFlow<Map<Int, HeroCategory>>(emptyMap())
  val quizAnswers: StateFlow<Map<Int, HeroCategory>> = _quizAnswers.asStateFlow()

  private val _quizResult = MutableStateFlow<QuizResult?>(null)
  val quizResult: StateFlow<QuizResult?> = _quizResult.asStateFlow()

  fun selectHero(hero: Hero) {
    _selectedHero.value = hero
  }

  fun selectRandomHero() {
    val otherHeroes = allHeroes.filter { it.id != _selectedHero.value.id }
    if (otherHeroes.isNotEmpty()) {
      _selectedHero.value = otherHeroes.random()
    }
  }

  fun resetToTodayHero() {
    _selectedHero.value = todayHero
  }

  fun toggleDeed(deedId: String, currentStatus: Boolean) {
    viewModelScope.launch {
      repository.toggleDeed(deedId, !currentStatus)
    }
  }

  fun toggleFavorite(heroId: String, currentFavorite: Boolean) {
    viewModelScope.launch {
      repository.toggleFavorite(heroId, !currentFavorite)
    }
  }

  fun honorHero(name: String, relation: String, reason: String, badgeName: String, onComplete: () -> Unit) {
    if (name.isBlank()) return
    viewModelScope.launch {
      repository.addHonoredHero(name, relation, reason, badgeName)
      onComplete()
    }
  }

  fun deleteHonoredHero(id: Long) {
    viewModelScope.launch {
      repository.deleteHonoredHero(id)
    }
  }

  fun answerQuiz(questionIndex: Int, category: HeroCategory) {
    val updated = _quizAnswers.value.toMutableMap()
    updated[questionIndex] = category
    _quizAnswers.value = updated

    if (questionIndex < quizQuestions.size - 1) {
      _currentQuizIndex.value = questionIndex + 1
    } else {
      // Calculate result
      val categoryCounts = updated.values.groupingBy { it }.eachCount()
      val topCategory = categoryCounts.maxByOrNull { it.value }?.key ?: HeroCategory.WISDOM
      val heroMatch = allHeroes.firstOrNull { it.category == topCategory } ?: allHeroes.first()

      val archetypeTitle = when (topCategory) {
        HeroCategory.SCIENCE -> "الباحث المستكشف والمبتكر"
        HeroCategory.HUMANITY -> "القلب الرحيم والملهم الإنساني"
        HeroCategory.COURAGE -> "الفارس الجسور وحامي الديار"
        HeroCategory.WISDOM -> "الحكيم صاحب الرؤية والمعلم"
        HeroCategory.EVERYDAY -> "بطل الواقع والصانع الصامت"
      }

      val desc = when (topCategory) {
        HeroCategory.SCIENCE -> "تمتلك عقلاً تحليلياً يبحث دائماً عن الأسباب والحلول، تلهمك المعرفة والإتقان."
        HeroCategory.HUMANITY -> "بطولتك تنبع من فيض إحساسك بالآخرين وعطائك الذي لا ينتظر مقابلاً."
        HeroCategory.COURAGE -> "روحك وثّابة لا تخشى المغامرة، وشجاعتك تقودك لنصرة الحق ومواجهة الصعاب."
        HeroCategory.WISDOM -> "تزن الأمور بحكمة ورصانة، وتبني أثراً راسخاً يمتد لأجيال."
        HeroCategory.EVERYDAY -> "أنت بطل المهام الصعبة التي تصنع فرقاً حقيقياً في حياة الناس كل يوم."
      }

      _quizResult.value = QuizResult(
        heroCategory = topCategory,
        matchedHero = heroMatch,
        archetypeTitle = archetypeTitle,
        description = desc
      )
    }
  }

  fun resetQuiz() {
    _currentQuizIndex.value = 0
    _quizAnswers.value = emptyMap()
    _quizResult.value = null
  }

  // ==========================================
  // --- Child Profiles & Preferences ---
  // ==========================================

  val activeChildren: StateFlow<List<ChildProfileEntity>> = repository.activeChildren.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allChildren: StateFlow<List<ChildProfileEntity>> = repository.allChildren.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  private val _selectedChildId = MutableStateFlow<String?>(repository.getSavedSelectedChildId())
  val selectedChildId: StateFlow<String?> = _selectedChildId.asStateFlow()

  val selectedChild: StateFlow<ChildProfileEntity?> = combine(activeChildren, _selectedChildId) { list, id ->
    if (id != null) {
      list.find { it.id == id } ?: list.firstOrNull()
    } else {
      list.firstOrNull()
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = null
  )

  private val _currentDateString = MutableStateFlow(timeProvider.todayDateString())
  val currentDateString: StateFlow<String> = _currentDateString.asStateFlow()

  fun refreshTodayDate() {
    val newDate = timeProvider.todayDateString()
    _currentDateString.value = newDate
    selectedChild.value?.let { child ->
      syncTodayTasksForChild(child.id)
    }
  }

  init {
    viewModelScope.launch {
      combine(selectedChild, _currentDateString) { child, date ->
        Pair(child, date)
      }.collect { (child, date) ->
        if (child != null) {
          repository.syncOccurrencesForChildAndDate(child.id, date)
        }
      }
    }

    // Auto-update date periodically (e.g. at midnight)
    viewModelScope.launch {
      while (true) {
        kotlinx.coroutines.delay(30_000)
        val latest = timeProvider.todayDateString()
        if (latest != _currentDateString.value) {
          _currentDateString.value = latest
          selectedChild.value?.let { child ->
            repository.syncOccurrencesForChildAndDate(child.id, latest)
          }
        }
      }
    }

    viewModelScope.launch {
      repository.ensureContentReviewCatalogPopulated()
    }
  }

  fun selectChild(childId: String) {
    _selectedChildId.value = childId
    repository.saveSelectedChildId(childId)
    syncTodayTasksForChild(childId)
  }

  fun createChild(alias: String, ageGroup: AgeGroup, avatarId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      val newId = repository.createChildProfile(alias, ageGroup, avatarId)
      _selectedChildId.value = newId
      syncTodayTasksForChild(newId)
      onComplete()
    }
  }

  fun updateChild(id: String, alias: String, ageGroup: AgeGroup, avatarId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.updateChildProfile(id, alias, ageGroup, avatarId)
      onComplete()
    }
  }

  fun archiveChild(id: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.archiveChildProfile(id)
      onComplete()
    }
  }

  // ==========================================
  // --- Parent Tasks & Occurrences ---
  // ==========================================

  val activeTasks: StateFlow<List<ParentTaskEntity>> = repository.activeTasks.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val allTasks: StateFlow<List<ParentTaskEntity>> = repository.allTasks.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val pendingApprovalOccurrences: StateFlow<List<TaskOccurrenceEntity>> = repository.pendingApprovalOccurrences.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  @OptIn(ExperimentalCoroutinesApi::class)
  val todayChildOccurrences: StateFlow<List<TaskOccurrenceEntity>> = combine(
    selectedChild,
    _currentDateString
  ) { child, dateStr ->
    Pair(child, dateStr)
  }.flatMapLatest { (child, dateStr) ->
    if (child == null) {
      flowOf(emptyList())
    } else {
      repository.getOccurrencesForChild(child.id, dateStr)
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun syncTodayTasksForChild(childId: String? = selectedChild.value?.id) {
    if (childId == null) return
    viewModelScope.launch {
      repository.syncOccurrencesForChildAndDate(childId, _currentDateString.value)
    }
  }

  // Praise message state
  private val _latestPraiseMessage = MutableStateFlow<String?>(null)
  val latestPraiseMessage: StateFlow<String?> = _latestPraiseMessage.asStateFlow()

  fun dismissPraiseMessage() {
    _latestPraiseMessage.value = null
  }

  // Error message state
  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  fun dismissErrorMessage() {
    _errorMessage.value = null
  }

  fun submitTaskCompletion(occurrenceId: String) {
    val childId = selectedChild.value?.id
    if (childId == null) {
      _errorMessage.value = "يرجى اختيار ملف الطفل أولاً"
      return
    }
    viewModelScope.launch {
      try {
        val updated = repository.submitChildTaskCompletion(occurrenceId, childId)
        if (updated.status == TaskOccurrenceStatus.COMPLETED.code) {
          _latestPraiseMessage.value = repository.getEffortPraiseForOccurrence(updated)
        }
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر إرسال إنجاز المهمة، يرجى المحاولة مرة أخرى."
      }
    }
  }

  fun submitTaskCompletion(occurrence: TaskOccurrenceEntity) {
    submitTaskCompletion(occurrence.id)
  }

  fun cancelTaskPendingApproval(occurrenceId: String) {
    val childId = selectedChild.value?.id
    if (childId == null) {
      _errorMessage.value = "يرجى اختيار ملف الطفل أولاً"
      return
    }
    viewModelScope.launch {
      try {
        repository.cancelChildTaskPendingApproval(occurrenceId, childId)
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "حدث خطأ أثناء إلغاء الانتظار"
      }
    }
  }

  fun skipTaskToday(occurrenceId: String) {
    val childId = selectedChild.value?.id
    if (childId == null) {
      _errorMessage.value = "يرجى اختيار ملف الطفل أولاً"
      return
    }
    viewModelScope.launch {
      try {
        repository.skipTaskToday(occurrenceId, childId)
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "حدث خطأ أثناء تخطي المهمة"
      }
    }
  }

  fun postponeTaskToday(occurrenceId: String, targetDateStr: String) {
    val childId = selectedChild.value?.id
    if (childId == null) {
      _errorMessage.value = "يرجى اختيار ملف الطفل أولاً"
      return
    }
    viewModelScope.launch {
      try {
        repository.postponeTaskToday(occurrenceId, childId, targetDateStr)
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "حدث خطأ أثناء تأجيل المهمة"
      }
    }
  }

  fun approveTaskOccurrence(occurrenceId: String) {
    viewModelScope.launch {
      repository.approveTaskOccurrence(occurrenceId)
      _latestPraiseMessage.value = repository.getRandomEffortPraise()
    }
  }

  fun retryTaskOccurrence(occurrenceId: String, gentleNote: String?) {
    viewModelScope.launch {
      repository.retryTaskOccurrence(occurrenceId, gentleNote)
    }
  }

  fun createParentTask(
    title: String,
    description: String,
    requiresApproval: Boolean,
    recurrenceType: RecurrenceType,
    targetDaysOfWeek: List<Int>,
    assignedChildIds: List<String>,
    startDate: String = repository.getTodayDateString(),
    ageGroup: String = "ALL",
    category: String = "GENERAL",
    onComplete: () -> Unit = {}
  ) {
    viewModelScope.launch {
      repository.createParentTask(
        title = title,
        description = description,
        requiresApproval = requiresApproval,
        recurrenceType = recurrenceType,
        targetDaysOfWeek = targetDaysOfWeek,
        assignedChildIds = assignedChildIds,
        startDate = startDate,
        ageGroup = ageGroup,
        category = category
      )
      selectedChild.value?.let { child ->
        if (assignedChildIds.contains(child.id)) {
          repository.syncOccurrencesForChildAndDate(child.id, repository.getTodayDateString())
        }
      }
      onComplete()
    }
  }

  fun updateParentTask(
    taskId: String,
    title: String,
    description: String,
    requiresApproval: Boolean,
    recurrenceType: RecurrenceType,
    targetDaysOfWeek: List<Int>,
    assignedChildIds: List<String>,
    startDate: String = repository.getTodayDateString(),
    ageGroup: String = "ALL",
    category: String = "GENERAL",
    onComplete: () -> Unit = {}
  ) {
    viewModelScope.launch {
      repository.updateParentTask(
        taskId = taskId,
        title = title,
        description = description,
        requiresApproval = requiresApproval,
        recurrenceType = recurrenceType,
        targetDaysOfWeek = targetDaysOfWeek,
        assignedChildIds = assignedChildIds,
        startDate = startDate,
        ageGroup = ageGroup,
        category = category
      )
      selectedChild.value?.let { child ->
        repository.syncOccurrencesForChildAndDate(child.id, repository.getTodayDateString())
      }
      onComplete()
    }
  }

  fun archiveParentTask(taskId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      repository.archiveParentTask(taskId)
      onComplete()
    }
  }

  suspend fun getAssignedChildIds(taskId: String): List<String> {
    return repository.getAssignedChildIdsForTask(taskId)
  }

  // ==========================================
  // --- Parent Gate & Security Session ---
  // ==========================================

  private val _isParentSessionUnlocked = MutableStateFlow(false)
  val isParentSessionUnlocked: StateFlow<Boolean> = _isParentSessionUnlocked.asStateFlow()

  suspend fun unlockParentSessionWithDeviceAuth(isDeviceAuthConfirmed: Boolean): Boolean {
    val success = repository.unlockWithDeviceAuth(isDeviceAuthConfirmed)
    if (success) {
      _isParentSessionUnlocked.value = true
    }
    return success
  }

  fun lockParentSession() {
    _isParentSessionUnlocked.value = false
    repository.lockParentSession()
  }

  suspend fun isPinConfigured(): Boolean {
    return repository.isPinConfigured()
  }

  suspend fun setupInitialPin(pin: String): Boolean {
    val success = repository.setupInitialPin(pin)
    if (success) {
      _isParentSessionUnlocked.value = true
    }
    return success
  }

  suspend fun verifyPin(enteredPin: String): HeroRepository.PinCheckResult {
    val result = repository.verifyPin(enteredPin)
    if (result is HeroRepository.PinCheckResult.Success) {
      _isParentSessionUnlocked.value = true
    }
    return result
  }

  suspend fun changePin(currentPin: String, newPin: String): Boolean {
    return repository.changePin(currentPin, newPin)
  }

  suspend fun resetPinWithDeviceAuth(newPin: String, isDeviceAuthConfirmed: Boolean): Boolean {
    val success = repository.resetPinWithDeviceAuth(newPin, isDeviceAuthConfirmed)
    if (success) {
      _isParentSessionUnlocked.value = true
    }
    return success
  }

  // ==========================================
  // --- Family Rewards & Moments ---
  // ==========================================

  @OptIn(ExperimentalCoroutinesApi::class)
  val childRewards: StateFlow<List<FamilyRewardEntity>> = selectedChildId.flatMapLatest { childId ->
    if (childId != null) {
      repository.getRewardsForChild(childId)
    } else {
      flowOf(emptyList())
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  @OptIn(ExperimentalCoroutinesApi::class)
  val activeChildRewards: StateFlow<List<FamilyRewardEntity>> = selectedChildId.flatMapLatest { childId ->
    if (childId != null) {
      repository.getActiveRewardsForChild(childId)
    } else {
      flowOf(emptyList())
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun createFamilyReward(
    title: String,
    description: String,
    rewardType: RewardType,
    childId: String,
    grantMode: RewardGrantMode,
    goalCriteria: String? = null,
    targetDate: String? = null,
    initialStatus: RewardStatus = RewardStatus.PLANNED,
    onComplete: () -> Unit = {}
  ) {
    viewModelScope.launch {
      try {
        repository.createFamilyReward(
          title = title,
          description = description,
          rewardType = rewardType,
          childId = childId,
          grantMode = grantMode,
          goalCriteria = goalCriteria,
          targetDate = targetDate,
          initialStatus = initialStatus
        )
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "حدث خطأ أثناء إضافة المكافأة"
      }
    }
  }

  fun updateFamilyReward(
    rewardId: String,
    title: String,
    description: String,
    rewardType: RewardType,
    childId: String,
    grantMode: RewardGrantMode,
    goalCriteria: String? = null,
    targetDate: String? = null,
    onComplete: () -> Unit = {}
  ) {
    viewModelScope.launch {
      try {
        repository.updateFamilyReward(
          rewardId = rewardId,
          title = title,
          description = description,
          rewardType = rewardType,
          childId = childId,
          grantMode = grantMode,
          goalCriteria = goalCriteria,
          targetDate = targetDate
        )
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "حدث خطأ أثناء تعديل المكافأة"
      }
    }
  }

  fun makeRewardAvailable(rewardId: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.makeRewardAvailable(rewardId)
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر إتاحة المكافأة"
      }
    }
  }

  fun requestRewardByChild(rewardId: String, onComplete: () -> Unit = {}) {
    val childId = selectedChild.value?.id ?: return
    viewModelScope.launch {
      try {
        repository.requestRewardByChild(rewardId, childId)
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر إرسال طلب المكافأة"
      }
    }
  }

  fun fulfillReward(rewardId: String, note: String? = null, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.fulfillReward(rewardId, note)
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر تسجيل إتمام المكافأة"
      }
    }
  }

  fun cancelReward(rewardId: String, gentleReason: String, onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        repository.cancelReward(rewardId, gentleReason)
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر إلغاء المكافأة"
      }
    }
  }

  suspend fun getRewardHistory(rewardId: String): List<FamilyRewardHistoryEntity> {
    return repository.getRewardHistory(rewardId)
  }

  // ==========================================
  // --- Weekly Summary ---
  // ==========================================

  private val _weeklySummary = MutableStateFlow<HeroRepository.WeeklySummaryData?>(null)
  val weeklySummary: StateFlow<HeroRepository.WeeklySummaryData?> = _weeklySummary.asStateFlow()

  fun loadWeeklySummary(childId: String, startDate: String, endDate: String) {
    viewModelScope.launch {
      try {
        val summary = repository.calculateWeeklySummary(childId, startDate, endDate)
        _weeklySummary.value = summary
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر تحميل الملخص الأسبوعي"
      }
    }
  }

  // ==========================================
  // --- Content Review Records ---
  // ==========================================

  val contentReviewRecords: StateFlow<List<ContentReviewRecordEntity>> = repository.getAllContentReviewRecords().stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun getContentReview(contentId: String) = repository.getContentReviewRecord(contentId)

  suspend fun getContentReviewSync(contentId: String) = repository.getContentReviewRecordSync(contentId)

  // ==========================================
  // --- App Settings ---
  // ==========================================

  val appSettings: StateFlow<AppSettingsEntity> = repository.getAppSettings().stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = AppSettingsEntity()
  )

  fun updateThemeMode(themeMode: String) {
    viewModelScope.launch {
      repository.updateThemeMode(themeMode)
    }
  }

  fun updateNotificationSettings(enabled: Boolean, startHour: Int, endHour: Int, hideOnLock: Boolean) {
    viewModelScope.launch {
      repository.updateNotificationSettings(enabled, startHour, endHour, hideOnLock)
    }
  }

  fun updateReduceMotion(reduce: Boolean) {
    viewModelScope.launch {
      repository.updateReduceMotion(reduce)
    }
  }

  // ==========================================
  // --- Backup, Restore & Deletion ---
  // ==========================================

  fun exportBackup(
    password: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val json = repository.exportEncryptedBackupJson(password)
        onSuccess(json)
      } catch (e: Exception) {
        onError(e.message ?: "تعذر تصدير النسخة الاحتياطية")
      }
    }
  }

  fun previewBackup(
    encryptedJson: String,
    password: String,
    onPreview: (HeroRepository.BackupPreviewInfo) -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val preview = repository.previewEncryptedBackup(encryptedJson, password)
        onPreview(preview)
      } catch (e: Exception) {
        onError(e.message ?: "تعذر قراءة النسخة الاحتياطية")
      }
    }
  }

  fun restoreBackup(
    encryptedJson: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val success = repository.restoreFromEncryptedBackup(encryptedJson, password)
        if (success) {
          // Re-sync current child
          selectedChild.value?.let { child ->
            syncTodayTasksForChild(child.id)
          }
          onSuccess()
        } else {
          onError("تعذر استعادة البيانات")
        }
      } catch (e: Exception) {
        onError(e.message ?: "حدث خطأ أثناء الاستعادة")
      }
    }
  }

  fun deleteChildPermanently(childId: String, onComplete: () -> Unit) {
    viewModelScope.launch {
      try {
        repository.deleteChildProfilePermanently(childId)
        if (_selectedChildId.value == childId) {
          _selectedChildId.value = null
        }
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر حذف ملف الطفل"
      }
    }
  }

  fun deleteAllFamilyData(onComplete: () -> Unit) {
    viewModelScope.launch {
      try {
        repository.deleteAllFamilyData()
        _selectedChildId.value = null
        onComplete()
      } catch (e: Exception) {
        _errorMessage.value = e.message ?: "تعذر حذف جميع البيانات"
      }
    }
  }

  class Factory(
    private val application: Application,
    private val customRepository: HeroRepository? = null,
    private val timeProvider: TimeProvider = DefaultTimeProvider()
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(HeroViewModel::class.java)) {
        return HeroViewModel(application, customRepository, timeProvider) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
