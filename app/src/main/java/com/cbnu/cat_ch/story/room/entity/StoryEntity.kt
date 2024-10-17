package com.cbnu.cat_ch.story.room.entity

// StoryEntity.kt
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storyTitle: String, // 이야기 제목
    val storyText: String, // 이야기 텍스트
    val storyImagePath: String?, // 이미지 경로
    val createdDate: Long, // 이야기 생성 날짜 (타임스탬프)
    val isFavorite: Boolean = false // 즐겨찾기 여부
)
