package com.batal.elyoum.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    HonoredHeroEntity::class,
    DailyDeedProgressEntity::class,
    FavoriteHeroEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class HeroDatabase : RoomDatabase() {
  abstract fun heroDao(): HeroDao

  companion object {
    @Volatile
    private var INSTANCE: HeroDatabase? = null

    fun getDatabase(context: Context): HeroDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          HeroDatabase::class.java,
          "hero_of_the_day_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
