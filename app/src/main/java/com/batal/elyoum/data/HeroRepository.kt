package com.batal.elyoum.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HeroRepository(context: Context) {
  private val heroDao: HeroDao = HeroDatabase.getDatabase(context).heroDao()

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

  fun getTodayDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    return sdf.format(Date())
  }

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
    val cleanAlias = alias.trim()
    require(cleanAlias.isNotEmpty()) { "اسم الطفل مطلوب" }
    heroDao.updateChildProfile(id, cleanAlias, ageGroup.code, avatarId)
  }

  suspend fun archiveChildProfile(id: String) {
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
    startDate: String = getTodayDateString()
  ): String {
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
      createdAtMillis = System.currentTimeMillis()
    )
    heroDao.insertTask(task)

    val assignments = assignedChildIds.distinct().map { childId ->
      TaskAssignmentEntity(
        id = java.util.UUID.randomUUID().toString(),
        taskId = taskId,
        childId = childId,
        createdAtMillis = System.currentTimeMillis()
      )
    }
    if (assignments.isNotEmpty()) {
      heroDao.insertAssignments(assignments)
    }
    return taskId
  }

  suspend fun updateParentTask(
    taskId: String,
    title: String,
    description: String,
    requiresApproval: Boolean,
    recurrenceType: RecurrenceType,
    targetDaysOfWeek: List<Int>,
    assignedChildIds: List<String>
  ) {
    val cleanTitle = title.trim()
    require(cleanTitle.isNotEmpty()) { "عنوان المهمة مطلوب" }
    heroDao.updateTask(
      taskId = taskId,
      title = cleanTitle,
      description = description.trim(),
      requiresApproval = requiresApproval,
      recurrenceType = recurrenceType.code,
      targetDaysOfWeek = targetDaysOfWeek.joinToString(",")
    )
    // Update assignments
    heroDao.deleteAssignmentsForTask(taskId)
    val assignments = assignedChildIds.distinct().map { childId ->
      TaskAssignmentEntity(
        id = java.util.UUID.randomUUID().toString(),
        taskId = taskId,
        childId = childId,
        createdAtMillis = System.currentTimeMillis()
      )
    }
    if (assignments.isNotEmpty()) {
      heroDao.insertAssignments(assignments)
    }
  }

  suspend fun archiveParentTask(taskId: String) {
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
   */
  suspend fun syncOccurrencesForChildAndDate(childId: String, dateStr: String = getTodayDateString()) {
    val childAssignments = heroDao.getAssignmentsForChild(childId)
    if (childAssignments.isEmpty()) return

    val calendar = Calendar.getInstance()
    // 1=Sunday, 2=Monday, ..., 7=Saturday
    val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

    for (assignment in childAssignments) {
      val task = heroDao.getTaskById(assignment.taskId) ?: continue
      if (task.isArchived) continue

      // Check date start
      if (task.startDate > dateStr) continue

      // Check recurrence applicability
      val isApplicableToday = when (task.recurrenceType) {
        RecurrenceType.ONCE.code -> (task.startDate == dateStr)
        RecurrenceType.DAILY.code -> true
        RecurrenceType.CUSTOM_DAYS.code -> {
          val days = task.targetDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
          days.contains(currentDayOfWeek)
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

  suspend fun submitChildTaskCompletion(occurrenceId: String, requiresApprovalSnapshot: Boolean) {
    val newStatus = if (requiresApprovalSnapshot) {
      TaskOccurrenceStatus.PENDING_APPROVAL.code
    } else {
      TaskOccurrenceStatus.COMPLETED.code
    }
    val now = System.currentTimeMillis()
    heroDao.updateOccurrenceStatus(occurrenceId, newStatus, completedAtMillis = now, updatedAtMillis = now)
  }

  suspend fun cancelChildTaskPendingApproval(occurrenceId: String) {
    val now = System.currentTimeMillis()
    heroDao.updateOccurrenceStatus(occurrenceId, TaskOccurrenceStatus.NOT_STARTED.code, completedAtMillis = null, updatedAtMillis = now)
  }

  suspend fun skipTaskToday(occurrenceId: String) {
    val now = System.currentTimeMillis()
    heroDao.updateOccurrenceStatus(occurrenceId, TaskOccurrenceStatus.SKIPPED.code, completedAtMillis = null, updatedAtMillis = now)
  }

  suspend fun approveTaskOccurrence(occurrenceId: String) {
    val now = System.currentTimeMillis()
    heroDao.reviewOccurrence(occurrenceId, TaskOccurrenceStatus.COMPLETED.code, note = null, updatedAtMillis = now)
  }

  suspend fun retryTaskOccurrence(occurrenceId: String, gentleNote: String?) {
    val now = System.currentTimeMillis()
    heroDao.reviewOccurrence(occurrenceId, TaskOccurrenceStatus.NOT_STARTED.code, note = gentleNote?.trim(), updatedAtMillis = now)
  }

  // ==========================================
  // --- Educational Effort Praise ---
  // ==========================================

  private val effortPraiseMessages = listOf(
    "رتّبت حاجتك بنفسك، شكرًا على اهتمامك ومسؤوليتك!",
    "محاولة رائعة واهتمام جميل، شكرًا لمجهودك الطيب اليوم!",
    "خطوة ممتازة نحو الاعتماد على نفسك، فخورون بجهدك!",
    "شكرًا لمبادرتك الطيبة، كل خطوة إيجابية تصنع فرقاً جميلاً!",
    "أنجزت خطوتك اليوم بهدوء وإتقان، بارك الله في سعيك!"
  )

  fun getRandomEffortPraise(): String = effortPraiseMessages.random()

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
    if (pin.length != 6 || !pin.all { it.isDigit() }) return false
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPin(pin, salt)
    val entity = ParentSecurityEntity(
      id = 1,
      pinSalt = salt,
      pinHash = hash,
      failedAttempts = 0,
      lockoutUntilMillis = 0L,
      isConfigured = true,
      updatedAtMillis = System.currentTimeMillis()
    )
    heroDao.insertOrUpdateParentSecurity(entity)
    return true
  }

  suspend fun verifyPin(enteredPin: String): PinCheckResult {
    val sec = heroDao.getParentSecurity() ?: return PinCheckResult.NotConfigured
    if (!sec.isConfigured) return PinCheckResult.NotConfigured

    val now = System.currentTimeMillis()
    if (sec.lockoutUntilMillis > now) {
      val remainingSeconds = (sec.lockoutUntilMillis - now + 999) / 1000
      return PinCheckResult.LockedOut(remainingSeconds)
    }

    val isValid = SecurityUtils.verifyPin(enteredPin, sec.pinSalt, sec.pinHash)
    if (isValid) {
      // Reset failed attempts on success
      heroDao.insertOrUpdateParentSecurity(
        sec.copy(failedAttempts = 0, lockoutUntilMillis = 0L, updatedAtMillis = now)
      )
      return PinCheckResult.Success
    } else {
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
  }

  suspend fun changePin(currentPin: String, newPin: String): Boolean {
    val verify = verifyPin(currentPin)
    if (verify !is PinCheckResult.Success) return false
    return setupInitialPin(newPin)
  }

  suspend fun resetPinWithDeviceAuth(newPin: String): Boolean {
    return setupInitialPin(newPin)
  }
}

