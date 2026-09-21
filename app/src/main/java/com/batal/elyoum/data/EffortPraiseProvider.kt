package com.batal.elyoum.data

object EffortPraiseProvider {

  fun getPraiseForTask(taskTitle: String?, taskDescription: String?): String {
    val title = (taskTitle ?: "").lowercase()
    val desc = (taskDescription ?: "").lowercase()
    val combined = "$title $desc"

    return when {
      // Room / bed / toys tidying
      combined.contains("سرير") || combined.contains("غرف") || combined.contains("ألعاب") || combined.contains("حذاء") || combined.contains("ترتيب") || combined.contains("تنظيم") -> {
        listOf(
          "رتبت حاجتك بنفسك، شكرًا على اهتمامك وتحملك المسؤولية!",
          "نظام جميل ولمسة مرتبة منك، فخورون بمبادرتك الطيبة!",
          "مكان مرتب يعكس اهتمامك الرائع، أحسنت صنعاً!"
        ).random()
      }

      // Reading / stories / learning
      combined.contains("قراء") || combined.contains("قصة") || combined.contains("كتاب") || combined.contains("صفح") -> {
        listOf(
          "قراءة ممتعة وتغذية جميلة لعقلك، أحسنت بالصبر والاستفادة!",
          "خطوة رائعة نحو عالم المعرفة، فخورون بحبك للقراءة!",
          "استثمار طيب في وقتك وعقلك، بارك الله في حرصك!"
        ).random()
      }

      // Prayer / remembrance / values
      combined.contains("صلا") || combined.contains("أذان") || combined.contains("مسجد") || combined.contains("قرآن") || combined.contains("أذكار") || combined.contains("دعاء") || combined.contains("حمد") -> {
        listOf(
          "تقبل الله طاعتك وبارك في حرصك وسعيك الطيب!",
          "خطوة مباركة تقربك من الخير، ثبتك الله وبارك فيك!",
          "نور وبركة في يومك بصلاتك وأذكارك، أحسنت!"
        ).random()
      }

      // Helping parents / meal / cooperation
      combined.contains("مساعد") || combined.contains("سفرة") || combined.contains("طعام") || combined.contains("أكل") || combined.contains("مطبخ") || combined.contains("إعداد") || combined.contains("والد") -> {
        listOf(
          "مساعدة لطيفة ويد بيضاء في بيتنا، شكرًا لتعاونك الجميل!",
          "بر وإحسان ومبادرة كريمة منك أسعدت قلوبنا، بارك الله فيك!",
          "روح التعاون تجعل كل عمل أحلى، شكرًا لجهدك الصادق!"
        ).random()
      }

      // Homework / studying
      combined.contains("واجب") || combined.contains("مدرس") || combined.contains("مكتب") || combined.contains("مذاكر") || combined.contains("درس") -> {
        listOf(
          "تركيز واجتهاد طيب في أداء واجبك، فخورون بمثابرتك!",
          "أنجزت ما عليك باهتمام وإتقان، خطوة ممتازة لنجاحك المستمر!",
          "صبر واجتهاد يثمر كل خير، أحسنت يا بطل!"
        ).random()
      }

      // Sport / health / hygiene
      combined.contains("رياض") || combined.contains("حرك") || combined.contains("مشي") || combined.contains("تمرين") || combined.contains("نظاف") || combined.contains("غسل") || combined.contains("أسنان") -> {
        listOf(
          "نشاط وحيوية واهتمام بصحتك وبدنك، أحسنت يا بطل!",
          "خطوة ممتازة لصحتك وقوتك، فخورون بنشاطك الإيجابي!",
          "نظافة وانتعاش يعكسان اهتمامك الطيب بنفسك!"
        ).random()
      }

      // Fallback: general encouraging effort praise without false claims
      else -> {
        listOf(
          "شكرًا على محاولتك واهتمامك ومجهودك الطيب اليوم!",
          "خطوة جميلة نحو الاعتماد على نفسك، فخورون بسعيك!",
          "كل محاولة طيبة تصنع فرقاً حقيقياً، شكرًا لمجهودك الصادق!"
        ).random()
      }
    }
  }
}
