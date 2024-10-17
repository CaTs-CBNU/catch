package com.cbnu.cat_ch.gallery.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.ItemDateHeaderBinding
import com.cbnu.cat_ch.databinding.ItemStoryBinding
import com.cbnu.cat_ch.gallery.data.StoryItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.File

class GalleryAdapter(private var context: Context, private var items: List<Any>, private val onItemClick: (StoryItem) -> Unit, private val actionModeCallback: ActionModeCallback) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface ActionModeCallback {
        fun onItemLongClicked(storyItem: StoryItem)
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItems(selected: List<StoryItem>) {
        selectedItems = selected.toMutableList()
        notifyDataSetChanged() // Refresh to show overlays on selected items
    }
    private var multiSelect = false // Multi-selection mode
    private var selectedItems: MutableList<StoryItem> = mutableListOf() // Track selected items

    // Long click listener interface
    private var onItemLongClickListener: ((StoryItem) -> Unit)? = null

    // Set long click listener
    fun setOnItemLongClickListener(listener: (StoryItem) -> Unit) {
        this.onItemLongClickListener = listener
    }

    // Clear selection
    fun clearSelection() {
        selectedItems.clear()
        multiSelect = false
        notifyDataSetChanged()
    }

    companion object {
        const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_STORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_DATE_HEADER else VIEW_TYPE_STORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateHeaderViewHolder(binding)
            }
            VIEW_TYPE_STORY -> {
                val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                StoryViewHolder(binding, context)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DateHeaderViewHolder -> holder.bind(items[position] as String)
            is StoryViewHolder -> holder.bind(
                items[position] as StoryItem,
                onItemClick,
                multiSelect,
                selectedItems.contains(items[position] as StoryItem)
            )
        }
    }

    // Start multi-select mode
    fun startMultiSelect() {
        multiSelect = true
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<StoryItem> {
        return selectedItems
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    // Toggle item selection
    fun toggleSelection(storyItem: StoryItem) {
        if (selectedItems.contains(storyItem)) {
            selectedItems.remove(storyItem)
        } else {
            selectedItems.add(storyItem)
        }
        notifyDataSetChanged() // Refresh UI based on selection state
    }

    class DateHeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(date: String) {
            binding.dateHeader.text = date
            binding.storyRecyclerView.adapter = null
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            storyItem: StoryItem,
            onItemClick: (StoryItem) -> Unit,
            multiSelect: Boolean,
            isSelected: Boolean
        ) {
            binding.storyTitle.text = storyItem.storyTitle
            binding.storyContent.text = storyItem.storyText

            // Show overlay when item is selected
            binding.selectionOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

            // Set default image if there’s no story image path
            if (storyItem.storyImagePath.isNullOrEmpty()) {
                binding.storyImage.setImageResource(R.drawable.resource_catch)
            } else {
                binding.storyImage.setImageURI(storyItem.storyImagePath.toUri())
            }

            // Set item click behavior
            binding.root.setOnClickListener {
                if (multiSelect) {
                    // If in multi-select mode, toggle selection

                    toggleSelection(storyItem)

                    actionModeCallback.onItemLongClicked(storyItem) // Notify fragment to start ActionMode
                } else {
                    // If not in multi-select, trigger onItemClick
                    onItemClick(storyItem)
                    val navController = binding.root.findNavController()
                    val bundle = Bundle().apply {
                        putParcelable("story_item", storyItem)
                    }
                    val navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.fade_in)
                        .setExitAnim(R.anim.fade_out)
                        .setPopEnterAnim(R.anim.fade_in)
                        .setPopExitAnim(R.anim.fade_out)
                        .build()
                    navController.navigate(
                        R.id.action_galleryStoriesFragment_to_galleryStoryDetailedFragment,
                        bundle,
                        navOptions
                    )
                }
            }

            // Long-click to start multi-select mode and notify fragment to start ActionMode
            binding.root.setOnLongClickListener {
                if (!multiSelect) {
                    startMultiSelect() // Start multi-select mode
                }
                toggleSelection(storyItem) // Select the long-clicked item
                actionModeCallback.onItemLongClicked(storyItem) // Notify fragment to start ActionMode
                true
            }
            // Share button functionality
            binding.shareButton.setOnClickListener {
                val bottomSheetDialog = BottomSheetDialog(context)
                val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_share_options, null)
                bottomSheetDialog.setContentView(view)

                // Handle "이미지 공유하기" click
                view.findViewById<TextView>(R.id.shareImage).setOnClickListener {
                    shareImage(storyItem.storyImagePath)
                    bottomSheetDialog.dismiss()
                }

                // Handle "스토리 공유하기" click
                view.findViewById<TextView>(R.id.shareStory).setOnClickListener {
                    shareStoryText(storyItem.storyText)
                    bottomSheetDialog.dismiss()
                }

                // Handle "닫기" click
                view.findViewById<TextView>(R.id.closeDialog).setOnClickListener {
                    bottomSheetDialog.dismiss()
                }

                bottomSheetDialog.show()
            }
        }
    }
    private fun shareImage(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            Toast.makeText(context, "이미지가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val imageUri: Uri = if (imagePath.startsWith("content://")) {
            // If it's already a content URI, use it directly
            Uri.parse(imagePath)
        } else {
            // If it's a file path, convert it to a content URI with FileProvider
            val imageFile = File(imagePath)
            if (!imageFile.exists()) {
                Toast.makeText(context, "이미지 파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                Log.e("GalleryAdapter", "File does not exist at path: $imagePath")
                return
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        }

        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "이미지 공유하기"))

        } catch (e: Exception) {
            Toast.makeText(context, "이미지 공유에 실패했습니다.", Toast.LENGTH_SHORT).show()
            Log.e("GalleryAdapter", "Error sharing image: ${e.message}", e)
        }
    }



    // Function to share the story text
    private fun shareStoryText(storyText: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, storyText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "스토리 공유하기"))
    }
}
