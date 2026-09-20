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
