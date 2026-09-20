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
}
