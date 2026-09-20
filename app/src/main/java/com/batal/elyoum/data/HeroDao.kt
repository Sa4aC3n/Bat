package com.batal.elyoum.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
