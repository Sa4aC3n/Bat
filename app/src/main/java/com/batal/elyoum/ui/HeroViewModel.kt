package com.batal.elyoum.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batal.elyoum.data.DailyDeed
import com.batal.elyoum.data.Hero
import com.batal.elyoum.data.HeroCategory
import com.batal.elyoum.data.HeroRepository
import com.batal.elyoum.data.HonoredHeroEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

class HeroViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = HeroRepository(application)

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
}
