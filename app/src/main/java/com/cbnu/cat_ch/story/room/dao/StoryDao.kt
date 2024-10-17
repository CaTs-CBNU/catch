package com.cbnu.cat_ch.story.room.dao

// StoryDao.kt
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cbnu.cat_ch.story.room.entity.StoryEntity

@Dao
interface StoryDao {
    @Insert
    suspend fun insert(story: StoryEntity)
//
//    @Query("SELECT * FROM story")
//    suspend fun getAllStories(): List<StoryEntity>

    @Query("SELECT * FROM story ORDER BY createdDate DESC")
    fun getAllStories(): LiveData<List<StoryEntity>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(story: StoryEntity)

    @Delete
    suspend fun delete(story: StoryEntity)

    @Query("DELETE FROM story WHERE id = :storyId")
    suspend fun deleteById(storyId: Long)

    @Query("DELETE FROM story WHERE id IN (:storyIds)")
    suspend fun deleteStoriesByIds(storyIds: List<Long>)

    @Query("SELECT * FROM story WHERE isFavorite = 0")
    fun getFavoriteStories(): LiveData<List<StoryEntity>>
}
