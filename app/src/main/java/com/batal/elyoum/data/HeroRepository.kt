package com.batal.elyoum.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HeroRepository(
  context: Context,
  dao: HeroDao? = null,
  private val timeProvider: TimeProvider = DefaultTimeProvider()
) {
  private val heroDao: HeroDao = dao ?: HeroDatabase.getDatabase(context).heroDao()

  @Volatile
  private var isParentSessionAuthenticated: Boolean = false

  fun isParentSessionActive(): Boolean = isParentSessionAuthenticated

  fun lockParentSession() {
    isParentSessionAuthenticated = false
  }

  private fun ensureParentSessionAuthenticated() {
    if (!isParentSessionAuthenticated) {
      throw SecurityException("عمليات قسم الوالدين تتطلب فتح جلسة مصادق عليها أولاً")
    }
  }

  suspend fun unlockWithDeviceAuth(isDeviceAuthConfirmed: Boolean): Boolean {
    if (!isDeviceAuthConfirmed) return false
    val sec = heroDao.getParentSecurity()
    if (sec == null || !sec.isConfigured) return false
    isParentSessionAuthenticated = true
    return true
  }

  val allHonoredHeroes: Flow<List<HonoredHeroEntity>> = heroDao.getAllHonoredHeroes()
  val favorites: Flow<List<FavoriteHeroEntity>> = heroDao.getAllFavorites()
  val totalDaysActive: Flow<Int> = heroDao.getCompletedDaysCount()

  companion object {
    val HEROES = listOf(
      Hero(
        id = "ibn_sina",
        name = "ابن سينا (أبو علي الحسين)",
        title = "الشيخ الرئيس وأمير الأطباء",
        category = HeroCategory.SCIENCE,
        era = "العصر الذهبي للعلوم - القرن الرابع الهجري",
        quote = "الوهم نصف الداء، والاطمئنان نصف الدواء، والصبر أول خطوات الشفاء.",
        story = "طبيب وفيلسوف موسوعي، ألّف كتاب 'القانون في الطب' الذي ظل المرجع الأساسي في جامعات أوروبا لقرون طويلة. كان يشخص الأمراض العصبية والنفسية ويربطها بالحالة الجسدية ببراعة فاقت عصره بألف عام.",
        virtues = listOf("العزيمة الخارقة", "الفضول المعرفي", "المثابرة في البحث"),
        dailyHeroicLesson = "كن شغوفاً بالتعلم المستمر، ووجّه علمك دائماً للتخفيف من آلام البشر وإدخال السكينة على قلوبهم.",
        iconName = "medical_services"
      ),
      Hero(
        id = "fatima_al_fihri",
        name = "فاطمة الفهرية (أم البنين)",
        title = "رائدة التعليم ومؤسسة أقدم جامعة في تاريخ الإنسانية",
        category = HeroCategory.WISDOM,
        era = "فاس، المغرب - 859 ميلادي",
        quote = "العلم أعظم صدقة جارية، والنور الذي نوقده بأموالنا وجهدنا يبقى يضيء بعد رحيلنا.",
        story = "هاجرت إلى فاس وسخّرت كامل ميراثها وثروتها لبناء جامع وجامعة القرويين. صامت طوال فترة بنائه شكراً لله، وفتحت أبواب العلم مجاناً لطلاب العلوم والطب والرياضيات، لتعترف بها اليونسكو كأقدم مؤسسة تعليمية جامعية مستمرة.",
        virtues = listOf("الإيثار العظيم", "بعد النظر والتخطيط", "الكرم الخالص"),
        dailyHeroicLesson = "استثمر وقتك ومواردك في تمكين الآخرين وبناء عقولهم؛ فالأثر الذي تتركه في الناس هو خلودك الحقيقي.",
        iconName = "school"
      ),
      Hero(
        id = "abbas_ibn_firnas",
        name = "عباس بن فرناس",
        title = "رائد الطيران والعبقري الأندلسي الجسور",
        category = HeroCategory.COURAGE,
        era = "قرطبة، الأندلس - القرن التاسع الميلادي",
        quote = "لا تخشَ السقوط في سبيل فكرة سامية، فكل محاولة شجاعة تمهد طريق التحليق للقادمين.",
        story = "عالم مخترع وشاعر وموسيقي. صمم أول محاولة طيران شراعي حقيقية مبنية على محاكاة حركة الطيور وحسابات دقيقة. كما ابتكر صناعة الزجاج من الحجارة وصنع أول ساعة مائية (الميقاتة) وأول قبة سماوية تمثيلية.",
        virtues = listOf("الجسارة العلمية", "الابتكار الجريء", "رفض المستحيل"),
        dailyHeroicLesson = "لا تدع الخوف من الفشل يمنعك من تجربة أفكارك الشجاعة. الخطوة الجريئة اليوم تصنع مستقبلاً جديداً.",
        iconName = "flight"
      ),
      Hero(
        id = "maryam_al_ijliya",
        name = "مريم العجلية (الأسطرلابية)",
        title = "عالمة الفلك وصانعة المعجزات الميكانيكية",
        category = HeroCategory.SCIENCE,
        era = "حلب، الشام - القرن العاشر الميلادي",
        quote = "دقة الصنعة وتأمل النجوم هما بوصلة الإنسان لمعرفة مكانه في هذا الكون الشاسع.",
        story = "تعلّمت صناعة الآلات الدقيقة في حلب، وبرعت في تطوير وتصنيع الإسطرلابات الفلكية المعقدة لتحديد المواقع وحركة الكواكب ومواقيت الصلاة، حتى أصبحت من أرفع العلماء مكانة في بلاط سيف الدولة الحمداني.",
        virtues = listOf("الدقة والإتقان", "الشغف العلمي", "التميز الهندسي"),
        dailyHeroicLesson = "أتقن ما تصنعه يديك إلى أقصى درجات الإتقان. الحرفية العالية وخدمة المجتمع شرف وبطولة.",
        iconName = "explore"
      ),
      Hero(
        id = "al_khwarizmi",
        name = "محمد بن موسى الخوارزمي",
        title = "مبتكر علم الجبر وواضع لبنات عالم الحوسبة",
        category = HeroCategory.WISDOM,
        era = "بيت الحكمة، بغداد - القرن الثالث الهجري",
        quote = "إذا كان الإنسان ذا خُلق فهو (1)، وإن كان ذا جمال فأضف صفراً (10)، وإن كان ذا مال فأضف صفراً (100)، فإن زال الخلق زال الواحد وبقيت الأصفار!",
        story = "أسس علم الجبر والمقابلة، وقدّم الأرقام الهندية-العربية ونظام الصفر إلى العالم. اسمه باللاتينية (Algorithm) أصبح الكلمة العالمية للوغاريتمات والخوارزميات التي تدير اليوم كل حاسوب وهاتف ذكي على وجه الأرض.",
        virtues = listOf("التفكير المنطقي البديع", "التبسيط الرياضي", "الأمانة الفكرية"),
        dailyHeroicLesson = "اجعل الأخلاق هي حجر الأساس لكل قدراتك ونجاحاتك، وابحث دائماً عن حلول تبسّط وتيسّر حياة الناس.",
        iconName = "calculate"
      ),
      Hero(
        id = "ibn_al_haytham",
        name = "الحسن بن الهيثم",
        title = "أبو البصريات ورائد المنهج التجريبي الحديث",
        category = HeroCategory.SCIENCE,
        era = "البصرة والقاهرة - القرن الرابع الهجري",
        quote = "الحق يُطلب لذاته، والباحث عن الحقيقة ليس من يتبع الهوى، بل من يجعل الشك والتجربة دليله.",
        story = "أول من شرح كيفية حدوث الرؤية بدخول الضوء إلى العين وليس خروجه منها، واخترع 'القمرة المظلمة' النواة الأولى للكاميرا الحديثة. وضع أصول البحث العلمي بالتجربة والملاحظة والتحقق قبل فرانسيس بيكون بقرون.",
        virtues = listOf("التحري الموضوعي", "النزاهة الفكرية", "الصبر التجريبي"),
        dailyHeroicLesson = "تحرَّ الحقيقة ولا تصدق كل ما يقال دون تدقيق، وكن شجاعاً في تصحيح المفاهيم الخاطئة بالحجة والبرهان.",
        iconName = "visibility"
      ),
      Hero(
        id = "ahmad_ibn_majid",
        name = "أحمد بن ماجد",
        title = "أسد البحار وعملاق الملاحة البحرية",
        category = HeroCategory.COURAGE,
        era = "رأس الخيمة، الخليج العربي - القرن الخامس عشر الميلادي",
        quote = "البحر لا يرحم الضعيف ولا الغافل، وسلاح البحّار يقظة الضمير وبراعة المعرفة بالرياح والنجوم.",
        story = "أشهر ملاح عربي، ألّف كتاب 'الفوائد في أصول علم البحر والقواعد'، واخترع 'الحقة' (البوصلة المغناطيسية الحديثة) وقاس ارتفاعات النجوم ببراعة لا نظير لها، مجتازاً أعتى المحيطات بمهارة أذهلت العالم.",
        virtues = listOf("رباطة الجأش", "الحكمة في مواجهة العواصف", "الريادة البحرية"),
        dailyHeroicLesson = "عندما تعصف بك أمواج الحياة ومشاكلها، تمسك بهدوئك وتوجيه بوصلتك نحو مبادئك دون تشتت.",
        iconName = "sailing"
      ),
      Hero(
        id = "everyday_medic",
        name = "طبيب الإنسانية والكوادر الطبية",
        title = "الجيش الأبيض وحراس الحياة",
        category = HeroCategory.EVERYDAY,
        era = "كل زمان ومكان - أبطال اللحظة الراهنة",
        quote = "إنقاذ روح واحدة هو بمثابة إحياء للإنسانية جمعاء.",
        story = "أطباء وممرضون يسهرون الليالي بين أروقة الطوارئ، يضحون براحتهم وسلامتهم الشخصية ليداوموا على نبض كل مريض ويمنحوا الأمل للعائلات الملهوفة. أبطال يعيشون بيننا بصمت ونبل.",
        virtues = listOf("الرحمة والإيثار", "الصمود المتواصل", "التفاني لإنقاذ الغير"),
        dailyHeroicLesson = "قدّم الرعاية والدعم لشخص يتألم، واشكر من يسهرون على راحتنا وسلامتنا في كل لحظة.",
        iconName = "favorite"
      ),
      Hero(
        id = "everyday_firefighter",
        name = "رجل الإطفاء والإنقاذ الشجاع",
        title = "منقذ الأرواح في قلب الخطر",
        category = HeroCategory.COURAGE,
        era = "حاضرنا الحي - أبطال الفداء اليومي",
        quote = "الشجاعة ليست غياب الخوف، بل الإقدام على النار لإنقاذ طفل أو أسرة تنتظر بارقة نجاة.",
        story = "رجال يدخلون إلى المباني المحترقة في اللحظة التي يفر منها الجميع، حاملين خراطيم المياه ومعدات التنفس، مواجهين الدخان والانهيار بروح فدائية لحماية الأرواح والممتلكات دون تردد.",
        virtues = listOf("التضحية الفدائية", "السرعة واليقظة", "نكران الذات"),
        dailyHeroicLesson = "كن سنداً لمن يحتاج النجدة، وقف بجرأة في مواجهة المصاعب ولا تتردد في نصرة المستضعفين.",
        iconName = "local_fire_department"
      ),
      Hero(
        id = "everyday_mother",
        name = "الأم العظيمة (صانعة الأبطال)",
        title = "ينبوع العطاء غير المشروط ومدرسة القيم",
        category = HeroCategory.HUMANITY,
        era = "كل بيت - قلب الأسرة النابض",
        quote = "الأمومة هي البطولة التي لا تطلب أوسمة، بل تكتفي برؤية أبنائها أصحاء وسعداء وناجحين.",
        story = "تستيقظ قبل الجميع وتنام بعد الجميع، تبث الحنان في الأرجاء، تسهر على المريض، وتغرس المبادئ والأخلاق بجهد يومي مستمر لا ينضب، لتصنع من أبنائها قادة ومصلحين ينفعون العالم.",
        virtues = listOf("الحب اللامشروط", "الصبر الذي لا ينفد", "التضحية العميقة"),
        dailyHeroicLesson = "أظهر الامتنان والبر لأمك وأهلك اليوم، واغرس الرحمة والوفاء في كل معاملة مع أقرب الناس إليك.",
        iconName = "volunteer_activism"
      )
    )

    val DEFAULT_DEEDS = listOf(
      DailyDeed(
        id = "help_others",
        title = "إغاثة أو مساعدة شخص",
        description = "ساعد شخصاً محتاجاً أو زميلاً في عمل أو جاراً ولو بعمل بسيط يُدخل السرور إلى قلبه.",
        points = 25,
        category = "إنسانية"
      ),
      DailyDeed(
        id = "learn_and_share",
        title = "اكتساب حكمة ومشاركتها",
        description = "تعلّم معلومة جديدة أو اقرأ فكرة قيّمة، وشاركها مع أهلك أو أصدقائك لنشر الوعي.",
        points = 20,
        category = "معرفة"
      ),
      DailyDeed(
        id = "moral_courage",
        title = "موقف شجاعة أخلاقية والتسامح",
        description = "اكظم غيظك عند الغضب، تسامح مع موقف مزعج، أو قل كلمة حق وابتسم بوجه من تلقاه.",
        points = 25,
        category = "شجاعة"
      ),
      DailyDeed(
        id = "honor_parents",
        title = "برّ وتقدير لمن تحب",
        description = "اتصل بوالديك أو شخص له فضل عليك، واشكره بصدق على ما قدّمه لك في مسيرتك.",
        points = 30,
        category = "وفاء"
      )
    )
  }

  fun getTodayDateString(): String = timeProvider.todayDateString()

  fun getCurrentTimeMillis(): Long = timeProvider.currentTimeMillis()

  fun getTodayHero(): Hero {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val index = (dayOfYear - 1) % HEROES.size
    return HEROES[index]
  }

  fun getHeroById(id: String): Hero? {
    return HEROES.find { it.id == id }
  }

  fun getDeedsForToday(): Flow<List<DailyDeedProgressEntity>> {
    return heroDao.getDeedsForDate(getTodayDateString())
  }

  suspend fun toggleDeed(deedId: String, isCompleted: Boolean) {
    val today = getTodayDateString()
    val key = "${today}_${deedId}"
    if (isCompleted) {
      heroDao.insertDeedProgress(
        DailyDeedProgressEntity(
          deedKey = key,
          dateStr = today,
          deedId = deedId,
          isCompleted = true
        )
      )
    } else {
      heroDao.deleteDeedProgress(key)
    }
  }

  suspend fun addHonoredHero(name: String, relation: String, reason: String, badgeName: String): Long {
    return heroDao.insertHonoredHero(
      HonoredHeroEntity(
        name = name.trim(),
        relation = relation.trim(),
        reason = reason.trim(),
        badgeName = badgeName
      )
    )
  }

  suspend fun deleteHonoredHero(id: Long) {
    heroDao.deleteHonoredHero(id)
  }

  suspend fun toggleFavorite(heroId: String, isFavorite: Boolean) {
    if (isFavorite) {
      heroDao.insertFavorite(FavoriteHeroEntity(heroId = heroId))
    } else {
      heroDao.deleteFavorite(heroId)
    }
  }

  // ==========================================
  // --- Child Profiles & Preferences ---
  // ==========================================

  private val sharedPrefs = context.getSharedPreferences("batal_prefs", Context.MODE_PRIVATE)
  private val KEY_SELECTED_CHILD_ID = "selected_child_id"

  val activeChildren: Flow<List<ChildProfileEntity>> = heroDao.getActiveChildren()
  val allChildren: Flow<List<ChildProfileEntity>> = heroDao.getAllChildren()

  fun getSavedSelectedChildId(): String? {
    return sharedPrefs.getString(KEY_SELECTED_CHILD_ID, null)
  }

  fun saveSelectedChildId(childId: String?) {
    sharedPrefs.edit().putString(KEY_SELECTED_CHILD_ID, childId).apply()
  }

  suspend fun createChildProfile(alias: String, ageGroup: AgeGroup, avatarId: String): String {
    ensureParentSessionAuthenticated()
    val cleanAlias = alias.trim()
    require(cleanAlias.isNotEmpty()) { "اسم الطفل مطلوب" }
    val id = java.util.UUID.randomUUID().toString()
    val child = ChildProfileEntity(
      id = id,
      alias = cleanAlias,
      ageGroup = ageGroup.code,
      avatarId = avatarId,
      createdAtMillis = System.currentTimeMillis(),
      isArchived = false
    )
    heroDao.insertChild(child)
    if (getSavedSelectedChildId() == null) {
      saveSelectedChildId(id)
    }
    return id
  }

  suspend fun updateChildProfile(id: String, alias: String, ageGroup: AgeGroup, avatarId: String) {
    ensureParentSessionAuthenticated()
    val cleanAlias = alias.trim()
    require(cleanAlias.isNotEmpty()) { "اسم الطفل مطلوب" }
    heroDao.updateChildProfile(id, cleanAlias, ageGroup.code, avatarId)
  }

  suspend fun archiveChildProfile(id: String) {
    ensureParentSessionAuthenticated()
    heroDao.archiveChild(id)
    if (getSavedSelectedChildId() == id) {
      saveSelectedChildId(null)
    }
  }

  suspend fun getChildById(id: String): ChildProfileEntity? {
    return heroDao.getChildById(id)
  }

  // ==========================================
  // --- Parent Tasks & Scheduling ---
  // ==========================================

  val activeTasks: Flow<List<ParentTaskEntity>> = heroDao.getActiveTasks()
  val allTasks: Flow<List<ParentTaskEntity>> = heroDao.getAllTasks()
  val pendingApprovalOccurrences: Flow<List<TaskOccurrenceEntity>> = heroDao.getPendingApprovalOccurrences()

  suspend fun createParentTask(
    title: String,
    description: String,
    requiresApproval: Boolean,
    recurrenceType: RecurrenceType,
    targetDaysOfWeek: List<Int>,
    assignedChildIds: List<String>,
    startDate: String = getTodayDateString(),
    ageGroup: String = "ALL",
    category: String = "GENERAL"
  ): String {
    ensureParentSessionAuthenticated()
    val cleanTitle = title.trim()
    require(cleanTitle.isNotEmpty()) { "عنوان المهمة مطلوب" }
    val taskId = java.util.UUID.randomUUID().toString()
    val task = ParentTaskEntity(
      id = taskId,
      title = cleanTitle,
      description = description.trim(),
      requiresApproval = requiresApproval,
      recurrenceType = recurrenceType.code,
      targetDaysOfWeek = targetDaysOfWeek.joinToString(","),
      startDate = startDate,
      isArchived = false,
      createdAtMillis = System.currentTimeMillis(),
      ageGroup = ageGroup,
      category = category
    )

    val assignments = assignedChildIds.distinct().map { childId ->
      TaskAssignmentEntity(
        id = java.util.UUID.randomUUID().toString(),
        taskId = taskId,
        childId = childId,
        createdAtMillis = System.currentTimeMillis()
      )
    }
    heroDao.insertTaskWithAssignments(task, assignments)
    return taskId
  }

  suspend fun updateParentTask(
    taskId: String,
    title: String,
    description: String,
    requiresApproval: Boolean,
    recurrenceType: RecurrenceType,
    targetDaysOfWeek: List<Int>,
    assignedChildIds: List<String>,
    startDate: String = getTodayDateString(),
    ageGroup: String = "ALL",
    category: String = "GENERAL"
  ) {
    ensureParentSessionAuthenticated()
    val cleanTitle = title.trim()
    require(cleanTitle.isNotEmpty()) { "عنوان المهمة مطلوب" }
    val task = ParentTaskEntity(
      id = taskId,
      title = cleanTitle,
      description = description.trim(),
      requiresApproval = requiresApproval,
      recurrenceType = recurrenceType.code,
      targetDaysOfWeek = targetDaysOfWeek.joinToString(","),
      startDate = startDate,
      isArchived = false,
      createdAtMillis = System.currentTimeMillis(),
      ageGroup = ageGroup,
      category = category
    )

    val assignments = assignedChildIds.distinct().map { childId ->
      TaskAssignmentEntity(
        id = java.util.UUID.randomUUID().toString(),
        taskId = taskId,
        childId = childId,
        createdAtMillis = System.currentTimeMillis()
      )
    }
    heroDao.updateTaskWithAssignments(task, assignments)
  }

  suspend fun archiveParentTask(taskId: String) {
    ensureParentSessionAuthenticated()
    heroDao.archiveTask(taskId)
  }

  suspend fun getAssignedChildIdsForTask(taskId: String): List<String> {
    return heroDao.getAssignmentsForTask(taskId).map { it.childId }
  }

  // ==========================================
  // --- Occurrences Generation & Lifecycle ---
  // ==========================================

  fun getOccurrencesForChild(childId: String, dateStr: String = getTodayDateString()): Flow<List<TaskOccurrenceEntity>> {
    return heroDao.getOccurrencesForChildAndDate(childId, dateStr)
  }

  /**
   * Synchronizes active parent tasks assigned to [childId] into [task_occurrences] for [dateStr].
   * Guarantees that duplicate occurrences are NEVER created for the same (taskId, childId, dateStr).
   * Relies on the occurrence/task date [dateStr] instead of device system clock to determine day of week.
   */
  suspend fun syncOccurrencesForChildAndDate(childId: String, dateStr: String = getTodayDateString()) {
    val childAssignments = heroDao.getAssignmentsForChild(childId)
    if (childAssignments.isEmpty()) return

    val dateCalendar = Calendar.getInstance().apply {
      try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
        if (parsed != null) {
          time = parsed
        }
      } catch (_: Exception) {
      }
    }
    // 1=Sunday, 2=Monday, ..., 7=Saturday
    val targetDayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK)

    for (assignment in childAssignments) {
      val task = heroDao.getTaskById(assignment.taskId) ?: continue
      if (task.isArchived) continue

      // Check date start
      if (task.startDate > dateStr) continue

      // Check recurrence applicability based on task date
      val isApplicableToday = when (task.recurrenceType) {
        RecurrenceType.ONCE.code -> (task.startDate == dateStr)
        RecurrenceType.DAILY.code -> true
        RecurrenceType.CUSTOM_DAYS.code -> {
          val days = task.targetDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
          days.contains(targetDayOfWeek)
        }
        else -> true
      }

      if (isApplicableToday) {
        val existing = heroDao.getOccurrence(task.id, childId, dateStr)
        if (existing == null) {
          val occurrence = TaskOccurrenceEntity(
            id = java.util.UUID.randomUUID().toString(),
            taskId = task.id,
            childId = childId,
            dateStr = dateStr,
            status = TaskOccurrenceStatus.NOT_STARTED.code,
            snapshotTitle = task.title,
            snapshotDescription = task.description,
            requiresApprovalSnapshot = task.requiresApproval,
            completedAtMillis = null,
            parentFeedbackNote = null,
            updatedAtMillis = System.currentTimeMillis()
          )
          heroDao.insertOccurrenceIfNotExists(occurrence)
        }
      }
    }
  }

  suspend fun submitChildTaskCompletion(identifier: String, selectedChildId: String): TaskOccurrenceEntity {
    require(selectedChildId.isNotBlank()) { "سياق الطفل المحدد مطلوب" }
    require(identifier.isNotBlank()) { "معرف المهمة مطلوب" }
    val now = getCurrentTimeMillis()
    val occById = heroDao.getOccurrenceById(identifier)
    return if (occById != null) {
      heroDao.submitOccurrenceAtomically(occById.id, selectedChildId, now)
    } else {
      heroDao.submitTaskByTaskIdAtomically(identifier, selectedChildId, getTodayDateString(), now)
    }
  }

  suspend fun cancelChildTaskPendingApproval(occurrenceId: String, selectedChildId: String) {
    val now = getCurrentTimeMillis()
    heroDao.cancelPendingApprovalAtomically(occurrenceId, selectedChildId, now)
  }

  suspend fun skipTaskToday(occurrenceId: String, selectedChildId: String) {
    val now = getCurrentTimeMillis()
    heroDao.skipOccurrenceAtomically(occurrenceId, selectedChildId, now)
  }

  suspend fun postponeTaskToday(occurrenceId: String, selectedChildId: String, targetDateStr: String): TaskOccurrenceEntity {
    require(selectedChildId.isNotBlank()) { "سياق الطفل المحدد مطلوب" }
    require(targetDateStr.isNotBlank()) { "تاريخ التأجيل مطلوب" }
    val now = getCurrentTimeMillis()
    return heroDao.postponeOccurrenceAtomically(occurrenceId, selectedChildId, targetDateStr, now)
  }

  suspend fun approveTaskOccurrence(occurrenceId: String) {
    ensureParentSessionAuthenticated()
    val now = System.currentTimeMillis()
    heroDao.reviewOccurrence(occurrenceId, TaskOccurrenceStatus.COMPLETED.code, note = null, updatedAtMillis = now)
  }

  suspend fun retryTaskOccurrence(occurrenceId: String, gentleNote: String?) {
    ensureParentSessionAuthenticated()
    val now = System.currentTimeMillis()
    heroDao.reviewOccurrence(occurrenceId, TaskOccurrenceStatus.NOT_STARTED.code, note = gentleNote?.trim(), updatedAtMillis = now)
  }

  // ==========================================
  // --- Educational Effort Praise ---
  // ==========================================

  fun getEffortPraiseForOccurrence(occurrence: TaskOccurrenceEntity): String {
    return EffortPraiseProvider.getPraiseForTask(occurrence.snapshotTitle, occurrence.snapshotDescription)
  }

  fun getRandomEffortPraise(): String = EffortPraiseProvider.getPraiseForTask(null, null)

  // ==========================================
  // --- Parent Security & PIN Gate ---
  // ==========================================

  val parentSecurityFlow: Flow<ParentSecurityEntity?> = heroDao.getParentSecurityFlow()

  sealed class PinCheckResult {
    object Success : PinCheckResult()
    data class IncorrectPin(val failedAttempts: Int, val lockoutSecondsRemaining: Long) : PinCheckResult()
    data class LockedOut(val secondsRemaining: Long) : PinCheckResult()
    object NotConfigured : PinCheckResult()
  }

  suspend fun isPinConfigured(): Boolean {
    val sec = heroDao.getParentSecurity()
    return sec != null && sec.isConfigured
  }

  suspend fun setupInitialPin(pin: String): Boolean {
    val existing = heroDao.getParentSecurity()
    if (existing != null && existing.isConfigured) {
      throw IllegalStateException("لا يمكن تعيين رمز جديد لأول مرة، يوجد رمز سري تم ضبطه مسبقاً")
    }
    if (!SecurityUtils.isValidPinFormat(pin)) return false
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(pin, salt)
    val entity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = hash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      algoVersion = SecurityUtils.CURRENT_ALGO_VERSION,
      iterations = SecurityUtils.DEFAULT_ITERATIONS,
      algorithm = SecurityUtils.DEFAULT_ALGORITHM,
      updatedAtMillis = getCurrentTimeMillis()
    )
    heroDao.insertOrUpdateParentSecurity(entity)
    isParentSessionAuthenticated = true
    return true
  }

  suspend fun verifyPin(enteredPin: String): PinCheckResult {
    val sec = heroDao.getParentSecurity() ?: return PinCheckResult.NotConfigured
    if (!sec.isConfigured) return PinCheckResult.NotConfigured

    val now = getCurrentTimeMillis()
    if (sec.lockoutUntilMillis > now) {
      val remainingSeconds = (sec.lockoutUntilMillis - now + 999) / 1000
      return PinCheckResult.LockedOut(remainingSeconds)
    }

    if (!SecurityUtils.isValidPinFormat(enteredPin)) {
      return recordFailedAttempt(sec, now)
    }

    // 1. Try recorded algorithm
    var isValid = SecurityUtils.verifyPin(
      enteredPin = enteredPin,
      saltBase64 = sec.pinSalt,
      expectedHash = sec.pinHash,
      algorithm = sec.algorithm,
      iterations = sec.iterations
    )

    var needsUpgrade = false

    // 2. If primary failed and record is legacy (algoVersion <= 1), try alternate legacy algorithm
    if (!isValid && sec.algoVersion <= SecurityUtils.LEGACY_ALGO_VERSION_1) {
      val altAlgorithm = if (sec.algorithm == SecurityUtils.DEFAULT_ALGORITHM) {
        SecurityUtils.LEGACY_ALGORITHM_SHA256_MULTI
      } else {
        SecurityUtils.DEFAULT_ALGORITHM
      }
      val legacyValid = SecurityUtils.verifyPin(
        enteredPin = enteredPin,
        saltBase64 = sec.pinSalt,
        expectedHash = sec.pinHash,
        algorithm = altAlgorithm,
        iterations = sec.iterations
      )
      if (legacyValid) {
        isValid = true
        needsUpgrade = true
      }
    } else if (isValid && (sec.algoVersion < SecurityUtils.CURRENT_ALGO_VERSION || sec.algorithm != SecurityUtils.DEFAULT_ALGORITHM)) {
      needsUpgrade = true
    }

    if (isValid) {
      if (needsUpgrade) {
        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPin(
          pin = enteredPin,
          saltBase64 = newSalt,
          algorithm = SecurityUtils.DEFAULT_ALGORITHM,
          iterations = SecurityUtils.DEFAULT_ITERATIONS
        )
        heroDao.insertOrUpdateParentSecurity(
          sec.copy(
            pinSalt = newSalt,
            pinHash = newHash,
            failedAttempts = 0,
            lockoutUntilMillis = 0L,
            algoVersion = SecurityUtils.CURRENT_ALGO_VERSION,
            iterations = SecurityUtils.DEFAULT_ITERATIONS,
            algorithm = SecurityUtils.DEFAULT_ALGORITHM,
            updatedAtMillis = now
          )
        )
      } else {
        heroDao.insertOrUpdateParentSecurity(
          sec.copy(failedAttempts = 0, lockoutUntilMillis = 0L, updatedAtMillis = now)
        )
      }
      isParentSessionAuthenticated = true
      return PinCheckResult.Success
    } else {
      return recordFailedAttempt(sec, now)
    }
  }

  private suspend fun recordFailedAttempt(sec: ParentSecurityEntity, now: Long): PinCheckResult {
    val newFailedAttempts = sec.failedAttempts + 1
    val lockoutDuration = SecurityUtils.getLockoutDurationMillis(newFailedAttempts)
    val newLockoutUntil = if (lockoutDuration > 0) now + lockoutDuration else 0L
    heroDao.insertOrUpdateParentSecurity(
      sec.copy(
        failedAttempts = newFailedAttempts,
        lockoutUntilMillis = newLockoutUntil,
        updatedAtMillis = now
      )
    )
    val lockoutSec = if (lockoutDuration > 0) lockoutDuration / 1000 else 0L
    return PinCheckResult.IncorrectPin(newFailedAttempts, lockoutSec)
  }

  suspend fun changePin(currentPin: String, newPin: String): Boolean {
    ensureParentSessionAuthenticated()
    val verify = verifyPin(currentPin)
    if (verify !is PinCheckResult.Success) return false
    if (!SecurityUtils.isValidPinFormat(newPin)) return false
    val now = getCurrentTimeMillis()
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(newPin, salt)
    val entity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = hash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      algoVersion = SecurityUtils.CURRENT_ALGO_VERSION,
      iterations = SecurityUtils.DEFAULT_ITERATIONS,
      algorithm = SecurityUtils.DEFAULT_ALGORITHM,
      updatedAtMillis = now
    )
    heroDao.insertOrUpdateParentSecurity(entity)
    isParentSessionAuthenticated = true
    return true
  }

  suspend fun resetPinWithDeviceAuth(newPin: String, isDeviceAuthConfirmed: Boolean): Boolean {
    if (!isDeviceAuthConfirmed) {
      throw SecurityException("إعادة ضبط الرمز تتطلب نجاح مصادقة أمان الجهاز")
    }
    if (!SecurityUtils.isValidPinFormat(newPin)) return false
    val now = getCurrentTimeMillis()
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(newPin, salt)
    val entity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = hash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      algoVersion = SecurityUtils.CURRENT_ALGO_VERSION,
      iterations = SecurityUtils.DEFAULT_ITERATIONS,
      algorithm = SecurityUtils.DEFAULT_ALGORITHM,
      updatedAtMillis = now
    )
    heroDao.insertOrUpdateParentSecurity(entity)
    isParentSessionAuthenticated = true
    return true
  }

  // ==========================================
  // --- Family Rewards & Moments ---
  // ==========================================

  fun getRewardsForChild(childId: String): Flow<List<FamilyRewardEntity>> = heroDao.getRewardsForChildFlow(childId)

  fun getActiveRewardsForChild(childId: String): Flow<List<FamilyRewardEntity>> = heroDao.getActiveRewardsForChildFlow(childId)

  suspend fun getRewardById(rewardId: String): FamilyRewardEntity? = heroDao.getRewardById(rewardId)

  suspend fun getRewardHistory(rewardId: String): List<FamilyRewardHistoryEntity> = heroDao.getHistoryForReward(rewardId)

  suspend fun createFamilyReward(
    title: String,
    description: String,
    rewardType: RewardType,
    childId: String,
    grantMode: RewardGrantMode,
    goalCriteria: String? = null,
    targetDate: String? = null,
    initialStatus: RewardStatus = RewardStatus.PLANNED
  ): String {
    ensureParentSessionAuthenticated()
    require(title.isNotBlank()) { "عنوان المكافأة مطلوب" }
    require(childId.isNotBlank()) { "الطفل المستفيد مطلوب" }
    val now = getCurrentTimeMillis()
    val rewardId = java.util.UUID.randomUUID().toString()
    val reward = FamilyRewardEntity(
      id = rewardId,
      title = title.trim(),
      description = description.trim(),
      rewardType = rewardType.code,
      childId = childId,
      grantMode = grantMode.code,
      goalCriteria = goalCriteria?.trim(),
      targetDate = targetDate?.trim(),
      status = initialStatus.code,
      cancellationReason = null,
      requestedAtMillis = null,
      fulfilledAtMillis = null,
      createdAtMillis = now,
      updatedAtMillis = now
    )
    heroDao.insertReward(reward)
    heroDao.insertRewardHistory(
      FamilyRewardHistoryEntity(
        rewardId = rewardId,
        fromStatus = "NONE",
        toStatus = initialStatus.code,
        note = "تم إنشاء لحظة/مكافأة أسرية جديدة",
        timestampMillis = now
      )
    )
    return rewardId
  }

  suspend fun updateFamilyReward(
    rewardId: String,
    title: String,
    description: String,
    rewardType: RewardType,
    childId: String,
    grantMode: RewardGrantMode,
    goalCriteria: String? = null,
    targetDate: String? = null
  ) {
    ensureParentSessionAuthenticated()
    val existing = heroDao.getRewardById(rewardId) ?: throw IllegalArgumentException("المكافأة غير موجودة")
    val now = getCurrentTimeMillis()
    val updated = existing.copy(
      title = title.trim(),
      description = description.trim(),
      rewardType = rewardType.code,
      childId = childId,
      grantMode = grantMode.code,
      goalCriteria = goalCriteria?.trim(),
      targetDate = targetDate?.trim(),
      updatedAtMillis = now
    )
    heroDao.updateReward(updated)
  }

  suspend fun makeRewardAvailable(rewardId: String): FamilyRewardEntity {
    ensureParentSessionAuthenticated()
    val now = getCurrentTimeMillis()
    return heroDao.makeRewardAvailableAtomically(rewardId, now)
  }

  suspend fun requestRewardByChild(rewardId: String, childId: String): FamilyRewardEntity {
    require(childId.isNotBlank()) { "معرف الطفل مطلوب" }
    val now = getCurrentTimeMillis()
    return heroDao.requestRewardAtomically(rewardId, childId, now)
  }

  suspend fun fulfillReward(rewardId: String, note: String? = null): FamilyRewardEntity {
    ensureParentSessionAuthenticated()
    val now = getCurrentTimeMillis()
    return heroDao.fulfillRewardAtomically(rewardId, note, now)
  }

  suspend fun cancelReward(rewardId: String, gentleReason: String): FamilyRewardEntity {
    ensureParentSessionAuthenticated()
    require(gentleReason.isNotBlank()) { "سبب الإلغاء اللطيف مطلوب لحفظ السجل بوضوح" }
    val now = getCurrentTimeMillis()
    return heroDao.cancelRewardAtomically(rewardId, gentleReason, now)
  }

  // ==========================================
  // --- Parent Weekly Summary ---
  // ==========================================

  data class WeeklySummaryData(
    val childId: String,
    val childAlias: String,
    val startDate: String,
    val endDate: String,
    val completedCount: Int,
    val pendingCount: Int,
    val postponedCount: Int,
    val skippedCount: Int,
    val incompleteCount: Int,
    val totalScheduled: Int,
    val fulfilledRewardsCount: Int,
    val fulfilledRewards: List<FamilyRewardEntity>,
    val categoriesParticipated: List<String>,
    val gentleSupportSuggestion: String,
    val disclaimer: String = "هذه اقتراحات داعمة وتوجيهية لتعزيز الترابط الأسري، وليست تقييماً نفسياً أو تشخيصاً متخصصاً."
  )

  suspend fun calculateWeeklySummary(childId: String, startDate: String, endDate: String): WeeklySummaryData {
    ensureParentSessionAuthenticated()
    val child = heroDao.getChildById(childId)
      ?: throw IllegalArgumentException("ملف الطفل غير موجود")

    val occurrences = heroDao.getOccurrencesForChildBetweenDatesSync(childId, startDate, endDate)

    val completed = occurrences.count { it.status == TaskOccurrenceStatus.COMPLETED.code }
    val pending = occurrences.count { it.status == TaskOccurrenceStatus.PENDING_APPROVAL.code }
    val postponed = occurrences.count { it.status == TaskOccurrenceStatus.POSTPONED.code }
    val skipped = occurrences.count { it.status == TaskOccurrenceStatus.SKIPPED.code }
    val incomplete = occurrences.count { it.status == TaskOccurrenceStatus.NOT_STARTED.code }
    val total = occurrences.size

    val startMillis = try {
      SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(startDate)?.time ?: 0L
    } catch (_: Exception) { 0L }
    val endMillis = try {
      (SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(endDate)?.time ?: 0L) + 86400000L
    } catch (_: Exception) { Long.MAX_VALUE }

    val fulfilledRewards = heroDao.getFulfilledRewardsForChildBetween(childId, startMillis, endMillis)

    // Discover categories participated based on completed tasks
    val completedTaskIds = occurrences.filter { it.status == TaskOccurrenceStatus.COMPLETED.code }.map { it.taskId }.distinct()
    val categories = mutableListOf<String>()
    for (tId in completedTaskIds) {
      val t = heroDao.getTaskById(tId)
      if (t != null && t.category.isNotBlank() && !categories.contains(t.category)) {
        categories.add(t.category)
      }
    }
    if (categories.isEmpty() && completed > 0) {
      categories.add("المبادرة والتعاون")
    }

    // Generate gentle rule-based suggestions
    val suggestion = when {
      total == 0 -> "لم يتم تسجيل مهام في هذا الأسبوع. يمكنكم اختيار مهمة أو اثنتين معاً لبدء أسبوع لطيف وهادئ."
      postponed + skipped > completed -> "لوحظ تأجيل أو تخطي بعض المهام؛ نقترح تقليل عدد المهام الأسبوع القادم واختيار مهام بسيطة ومحببة للطفل لتعزيز ثقته بنفسه."
      completed >= (total * 0.7) -> "أسبوع رائع ومثمر بخطوات طيبة وملموسة! شكرًا لدعمكم المستمر وتشجيعكم لجهود البطل الصغير."
      else -> "أسبوع طيب مليء بالمحاولات والتعلم؛ استمروا في تشجيع كل خطوة إيجابية والاحتفال بالسعي الطيب."
    }

    return WeeklySummaryData(
      childId = childId,
      childAlias = child.alias,
      startDate = startDate,
      endDate = endDate,
      completedCount = completed,
      pendingCount = pending,
      postponedCount = postponed,
      skippedCount = skipped,
      incompleteCount = incomplete,
      totalScheduled = total,
      fulfilledRewardsCount = fulfilledRewards.size,
      fulfilledRewards = fulfilledRewards,
      categoriesParticipated = categories,
      gentleSupportSuggestion = suggestion
    )
  }

  // ==========================================
  // --- Editorial Content Review Catalog ---
  // ==========================================

  suspend fun ensureContentReviewCatalogPopulated() {
    if (heroDao.countContentReviewRecords() == 0) {
      heroDao.insertContentReviewRecords(ContentReviewCatalog.DEFAULT_RECORDS)
    }
  }

  fun getAllContentReviewRecords(): Flow<List<ContentReviewRecordEntity>> = heroDao.getAllContentReviewRecordsFlow()

  fun getContentReviewRecord(contentId: String): Flow<ContentReviewRecordEntity?> = heroDao.getContentReviewRecordFlow(contentId)

  suspend fun getContentReviewRecordSync(contentId: String): ContentReviewRecordEntity? = heroDao.getContentReviewRecord(contentId)

  // ==========================================
  // --- App Settings & Customization ---
  // ==========================================

  fun getAppSettings(): Flow<AppSettingsEntity> = heroDao.getAppSettingsFlow().map { it ?: AppSettingsEntity() }

  suspend fun updateThemeMode(themeMode: String) {
    val current = heroDao.getAppSettingsSync() ?: AppSettingsEntity()
    heroDao.insertOrUpdateAppSettings(current.copy(themeMode = themeMode, updatedAtMillis = getCurrentTimeMillis()))
  }

  suspend fun updateNotificationSettings(enabled: Boolean, startHour: Int, endHour: Int, hideOnLock: Boolean) {
    val current = heroDao.getAppSettingsSync() ?: AppSettingsEntity()
    heroDao.insertOrUpdateAppSettings(
      current.copy(
        notificationsEnabled = enabled,
        notificationQuietHourStart = startHour,
        notificationQuietHourEnd = endHour,
        hideTaskDetailsOnLockScreen = hideOnLock,
        updatedAtMillis = getCurrentTimeMillis()
      )
    )
  }

  suspend fun updateReduceMotion(reduce: Boolean) {
    val current = heroDao.getAppSettingsSync() ?: AppSettingsEntity()
    heroDao.insertOrUpdateAppSettings(current.copy(reduceMotionCelebration = reduce, updatedAtMillis = getCurrentTimeMillis()))
  }

  // ==========================================
  // --- Data Backup, Restore & Cascade Deletion ---
  // ==========================================

  data class BackupPreviewInfo(
    val childCount: Int,
    val taskCount: Int,
    val occurrenceCount: Int,
    val rewardCount: Int,
    val schemaVersion: Int,
    val createdAt: String
  )

  suspend fun exportEncryptedBackupJson(backupPassword: String): String {
    ensureParentSessionAuthenticated()
    require(backupPassword.length >= 4) { "يجب أن لا تقل كلمة مرور النسخة الاحتياطية عن 4 خانات" }

    val children = heroDao.getAllChildrenSync()
    val tasks = heroDao.getAllTasksSync()
    val occurrences = heroDao.getAllOccurrencesSync()
    val rewards = heroDao.getAllRewardsSync()

    val root = JSONObject().apply {
      put("version", 4)
      put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))

      val childrenArray = JSONArray()
      for (c in children) {
        childrenArray.put(JSONObject().apply {
          put("id", c.id)
          put("alias", c.alias)
          put("ageGroup", c.ageGroup)
          put("avatarId", c.avatarId)
          put("createdAtMillis", c.createdAtMillis)
          put("isArchived", c.isArchived)
        })
      }
      put("children", childrenArray)

      val tasksArray = JSONArray()
      for (t in tasks) {
        tasksArray.put(JSONObject().apply {
          put("id", t.id)
          put("title", t.title)
          put("description", t.description)
          put("requiresApproval", t.requiresApproval)
          put("recurrenceType", t.recurrenceType)
          put("targetDaysOfWeek", t.targetDaysOfWeek)
          put("startDate", t.startDate)
          put("isArchived", t.isArchived)
          put("createdAtMillis", t.createdAtMillis)
          put("ageGroup", t.ageGroup)
          put("category", t.category)
        })
      }
      put("tasks", tasksArray)

      val occurrencesArray = JSONArray()
      for (o in occurrences) {
        occurrencesArray.put(JSONObject().apply {
          put("id", o.id)
          put("taskId", o.taskId)
          put("childId", o.childId)
          put("dateStr", o.dateStr)
          put("status", o.status)
          put("snapshotTitle", o.snapshotTitle)
          put("snapshotDescription", o.snapshotDescription)
          put("requiresApprovalSnapshot", o.requiresApprovalSnapshot)
          put("completedAtMillis", o.completedAtMillis ?: JSONObject.NULL)
          put("parentFeedbackNote", o.parentFeedbackNote ?: JSONObject.NULL)
          put("updatedAtMillis", o.updatedAtMillis)
          put("postponedToDate", o.postponedToDate ?: JSONObject.NULL)
          put("originalOccurrenceId", o.originalOccurrenceId ?: JSONObject.NULL)
        })
      }
      put("occurrences", occurrencesArray)

      val rewardsArray = JSONArray()
      for (r in rewards) {
        rewardsArray.put(JSONObject().apply {
          put("id", r.id)
          put("title", r.title)
          put("description", r.description)
          put("rewardType", r.rewardType)
          put("childId", r.childId)
          put("grantMode", r.grantMode)
          put("goalCriteria", r.goalCriteria ?: JSONObject.NULL)
          put("targetDate", r.targetDate ?: JSONObject.NULL)
          put("status", r.status)
          put("cancellationReason", r.cancellationReason ?: JSONObject.NULL)
          put("requestedAtMillis", r.requestedAtMillis ?: JSONObject.NULL)
          put("fulfilledAtMillis", r.fulfilledAtMillis ?: JSONObject.NULL)
          put("createdAtMillis", r.createdAtMillis)
          put("updatedAtMillis", r.updatedAtMillis)
        })
      }
      put("rewards", rewardsArray)
    }

    return BackupEncryptionUtils.encryptBackup(root.toString(), backupPassword)
  }

  suspend fun previewEncryptedBackup(encryptedEnvelopeJson: String, backupPassword: String): BackupPreviewInfo {
    ensureParentSessionAuthenticated()
    val plainJson = BackupEncryptionUtils.decryptBackup(encryptedEnvelopeJson, backupPassword)
    val root = JSONObject(plainJson)
    val version = root.optInt("version", 4)
    val exportedAt = root.optString("exportedAt", "")
    val childCount = root.optJSONArray("children")?.length() ?: 0
    val taskCount = root.optJSONArray("tasks")?.length() ?: 0
    val occCount = root.optJSONArray("occurrences")?.length() ?: 0
    val rewardCount = root.optJSONArray("rewards")?.length() ?: 0

    return BackupPreviewInfo(
      childCount = childCount,
      taskCount = taskCount,
      occurrenceCount = occCount,
      rewardCount = rewardCount,
      schemaVersion = version,
      createdAt = exportedAt
    )
  }

  suspend fun restoreFromEncryptedBackup(encryptedEnvelopeJson: String, backupPassword: String): Boolean {
    ensureParentSessionAuthenticated()
    val plainJson = BackupEncryptionUtils.decryptBackup(encryptedEnvelopeJson, backupPassword)
    val root = JSONObject(plainJson)

    val childrenArray = root.optJSONArray("children") ?: JSONArray()
    val tasksArray = root.optJSONArray("tasks") ?: JSONArray()
    val occurrencesArray = root.optJSONArray("occurrences") ?: JSONArray()
    val rewardsArray = root.optJSONArray("rewards") ?: JSONArray()

    // Atomically clear and restore without touching security PIN credentials
    heroDao.deleteAllFamilyDataAtomically()

    for (i in 0 until childrenArray.length()) {
      val cObj = childrenArray.getJSONObject(i)
      val child = ChildProfileEntity(
        id = cObj.getString("id"),
        alias = cObj.getString("alias"),
        ageGroup = cObj.getString("ageGroup"),
        avatarId = cObj.getString("avatarId"),
        createdAtMillis = cObj.getLong("createdAtMillis"),
        isArchived = cObj.optBoolean("isArchived", false)
      )
      heroDao.insertChild(child)
    }

    for (i in 0 until tasksArray.length()) {
      val tObj = tasksArray.getJSONObject(i)
      val task = ParentTaskEntity(
        id = tObj.getString("id"),
        title = tObj.getString("title"),
        description = tObj.getString("description"),
        requiresApproval = tObj.getBoolean("requiresApproval"),
        recurrenceType = tObj.getString("recurrenceType"),
        targetDaysOfWeek = tObj.optString("targetDaysOfWeek", ""),
        startDate = tObj.getString("startDate"),
        isArchived = tObj.optBoolean("isArchived", false),
        createdAtMillis = tObj.getLong("createdAtMillis"),
        ageGroup = tObj.optString("ageGroup", "ALL"),
        category = tObj.optString("category", "GENERAL")
      )
      heroDao.insertTask(task)
    }

    for (i in 0 until occurrencesArray.length()) {
      val oObj = occurrencesArray.getJSONObject(i)
      val occ = TaskOccurrenceEntity(
        id = oObj.getString("id"),
        taskId = oObj.getString("taskId"),
        childId = oObj.getString("childId"),
        dateStr = oObj.getString("dateStr"),
        status = oObj.getString("status"),
        snapshotTitle = oObj.getString("snapshotTitle"),
        snapshotDescription = oObj.getString("snapshotDescription"),
        requiresApprovalSnapshot = oObj.getBoolean("requiresApprovalSnapshot"),
        completedAtMillis = if (oObj.isNull("completedAtMillis")) null else oObj.getLong("completedAtMillis"),
        parentFeedbackNote = if (oObj.isNull("parentFeedbackNote")) null else oObj.getString("parentFeedbackNote"),
        updatedAtMillis = oObj.getLong("updatedAtMillis"),
        postponedToDate = if (oObj.isNull("postponedToDate")) null else oObj.getString("postponedToDate"),
        originalOccurrenceId = if (oObj.isNull("originalOccurrenceId")) null else oObj.getString("originalOccurrenceId")
      )
      heroDao.insertOccurrenceIfNotExists(occ)
    }

    for (i in 0 until rewardsArray.length()) {
      val rObj = rewardsArray.getJSONObject(i)
      val reward = FamilyRewardEntity(
        id = rObj.getString("id"),
        title = rObj.getString("title"),
        description = rObj.getString("description"),
        rewardType = rObj.getString("rewardType"),
        childId = rObj.getString("childId"),
        grantMode = rObj.getString("grantMode"),
        goalCriteria = if (rObj.isNull("goalCriteria")) null else rObj.getString("goalCriteria"),
        targetDate = if (rObj.isNull("targetDate")) null else rObj.getString("targetDate"),
        status = rObj.getString("status"),
        cancellationReason = if (rObj.isNull("cancellationReason")) null else rObj.getString("cancellationReason"),
        requestedAtMillis = if (rObj.isNull("requestedAtMillis")) null else rObj.getLong("requestedAtMillis"),
        fulfilledAtMillis = if (rObj.isNull("fulfilledAtMillis")) null else rObj.getLong("fulfilledAtMillis"),
        createdAtMillis = rObj.getLong("createdAtMillis"),
        updatedAtMillis = rObj.getLong("updatedAtMillis")
      )
      heroDao.insertReward(reward)
    }

    return true
  }

  suspend fun deleteChildProfilePermanently(childId: String) {
    ensureParentSessionAuthenticated()
    heroDao.deleteChildPermanentlyAtomically(childId)
  }

  suspend fun deleteAllFamilyData() {
    ensureParentSessionAuthenticated()
    heroDao.deleteAllFamilyDataAtomically()
  }
}

