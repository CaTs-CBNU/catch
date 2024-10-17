package com.cbnu.cat_ch.gallery.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryItem(
    val id: Long,
    val storyTitle: String,      // Story title
    val storyText: String,       // Story text
    val storyImagePath: String?, // Path to image
    val createdDate: Long,       // Timestamp
    val formattedDate: String,   // Formatted date string
    var isFavorite: Boolean,      // Favorite status
    var isSelected: Boolean = false
) : Parcelable