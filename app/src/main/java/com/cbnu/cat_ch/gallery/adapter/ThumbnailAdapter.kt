package com.cbnu.cat_ch.gallery.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.gallery.data.StoryItem

class ThumbnailAdapter(
    private val storyItems: List<StoryItem>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder>() {

    private var selectedIndex = 0

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        notifyDataSetChanged() // Update selected state of thumbnails
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_thumbnail, parent, false)
        return ThumbnailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbnailViewHolder, position: Int) {
        val storyItem = storyItems[position]
        holder.bind(storyItem, position == selectedIndex, position) {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = storyItems.size

    inner class ThumbnailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val thumbnailImage: ImageView = view.findViewById(R.id.thumbnailImage)
        private val thumbnailNumber: TextView = view.findViewById(R.id.thumbnailNumber)

        @SuppressLint("SetTextI18n")
        fun bind(storyItem: StoryItem, isSelected: Boolean, position: Int, onClick: () -> Unit) {
            storyItem.storyImagePath?.let {
                thumbnailImage.setImageURI(it.toUri())
            } ?: thumbnailImage.setImageResource(R.drawable.background)

            // Set rounded corners using Glide
            val cornerRadius = 16 // Adjust this value for the roundness of corners

            Glide.with(itemView.context)
                .load(storyItem.storyImagePath?.toUri())
                .apply(RequestOptions().transform(RoundedCorners(cornerRadius)))
                .placeholder(R.drawable.background) // Optional placeholder
                .into(thumbnailImage)

            // Set number below the image
            thumbnailNumber.text = (position + 1).toString() // Display position + 1 to start from 1

            // Highlight selected thumbnail
            thumbnailImage.alpha = if (isSelected) 1.0f else 0.5f

            itemView.setOnClickListener { onClick() }
        }
    }

    // 아이템의 위치를 이동시킵니다.
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                java.util.Collections.swap(storyItems, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                java.util.Collections.swap(storyItems, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

}
