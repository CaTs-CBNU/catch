package com.cbnu.cat_ch.gallery.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentGalleryStoryDetailedBinding
import com.cbnu.cat_ch.gallery.data.StoryItem
import com.cbnu.cat_ch.story.room.dao.StoryDao
import com.cbnu.cat_ch.story.room.db.StoryDatabase
import com.cbnu.cat_ch.story.room.entity.StoryEntity
import com.example.awesomedialog.AwesomeDialog
import com.example.awesomedialog.body
import com.example.awesomedialog.icon
import com.example.awesomedialog.onPositive
import com.example.awesomedialog.title
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryStoryDetailedFragment : Fragment() {
    private lateinit var binding: FragmentGalleryStoryDetailedBinding
    private lateinit var storyItem: StoryItem
    private lateinit var storyDao: StoryDao
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryStoryDetailedBinding.inflate(layoutInflater)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        storyDao = StoryDatabase.getDatabase(requireContext()).storyDao()
        storyItem = arguments?.getParcelable("story_item") ?: return

        Log.d("GalleryStoryDetailedFragment", "Loaded storyItem: $storyItem")

        val titleCharCountTextView = binding.titleCharCount
        val descriptionCharCountTextView = binding.descriptionCharCount
        val defaultTextColor = binding.storyTitle.currentTextColor

        // TextWatcher for title EditText

        binding.storyTitle.setText(storyItem.storyTitle)
        binding.storyDescription.setText(storyItem.storyText)
        binding.storyImage.setImageURI(storyItem.storyImagePath?.toUri())


        titleCharCountTextView.text = "${binding.storyTitle.text.length}/30"
        descriptionCharCountTextView.text = "${binding.storyDescription.text.length}/800"
        if (binding.storyTitle.text.length > 30) {
            binding.storyTitle.setTextColor(Color.RED)
        }
        if (binding.storyDescription.text.length > 800) {
            binding.storyDescription.setTextColor(Color.RED)
        }

        updateBookmarkIcon()
        // 제목 글자 수 업데이트
        binding.storyTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                binding.titleCharCount.text = "$charCount/30"

                // 글자 수 초과 시 텍스트 색상 변경
                if (charCount > 30) {
                    binding.storyTitle.setTextColor(Color.RED)
                } else {
                    binding.storyTitle.setTextColor(defaultTextColor)
                }
            }
        })

        // 설명 글자 수 업데이트
        binding.storyDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                binding.descriptionCharCount.text = "$charCount/800"

                // 글자 수 초과 시 텍스트 색상 변경
                if (charCount > 800) {
                    binding.storyDescription.setTextColor(Color.RED)
                } else {
                    binding.storyDescription.setTextColor(defaultTextColor)
                }
            }
        })

        // 제목 수정 시 업데이트
        binding.storyTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                storyItem = storyItem.copy(storyTitle = binding.storyTitle.text.toString())
                updateStoryItemInDatabase(storyItem)
            }
        }

        // 설명 수정 시 업데이트
        binding.storyDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                storyItem = storyItem.copy(storyText = binding.storyDescription.text.toString())
                updateStoryItemInDatabase(storyItem)
            }
        }


        binding.storyImage.setOnClickListener {
            openImagePicker()
        }

        binding.bookmarkIcon.setOnClickListener {
            showBookmarkConfirmationDialog()
        }

        binding.deleteIcon.setOnClickListener {
            showDeleteConfirmationDialog(storyId = storyItem.id)
        }

        binding.shareIcon.setOnClickListener {
            shareStory()
        }

        binding.downloadIcon.setOnClickListener {
            val titleCharCount = binding.storyTitle.text.length
            val descriptionCharCount = binding.storyDescription.text.length

            if (titleCharCount > 30 || descriptionCharCount > 800) {
                showLimitExceededDialog()
            } else {
                // Update storyItem with the latest text before navigating
                storyItem = storyItem.copy(
                    storyTitle = binding.storyTitle.text.toString(),
                    storyText = binding.storyDescription.text.toString()
                )

                // Save updated storyItem to the database
                lifecycleScope.launch {
                    updateStoryItemInDatabase(storyItem)
                    // Once saved, proceed to download dialog
                    showDownloadConfirmationDialog()
                }
            }
        }

    }
    private fun showLimitExceededDialog() {
        AwesomeDialog.build(requireActivity())
            .title("알림", titleColor = R.color.black)
            .body("제목은 최대 30자, 설명은 최대 800자까지 입력할 수 있습니다.")
            .icon(R.drawable.baseline_info_outline)
            .onPositive("확인")
            .show()
    }


    private fun showDownloadConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Info!")
            .setMessage("Pdf로 생성하시겠습니까??")
            .setPositiveButton("네") { _, _ -> navigateToPdfDownloadFragment() }
            .setNegativeButton("아니오", null)
            .show()
    }
    private fun navigateToPdfDownloadFragment() {
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
            R.id.action_galleryStoryDetailedFragment_to_galleryPdfGeneratorFragment,
            bundle,
            navOptions
        )
    }





    private fun shareStory() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_share_options, null)
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

    private fun showDeleteConfirmationDialog(storyId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("삭제 확인")
            .setMessage("이야기를 삭제하시겠습니까?")
            .setPositiveButton("예") { _, _ -> deleteStory(storyId) }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun deleteStory(storyId: Long) {
        lifecycleScope.launch {
            try {
                storyDao.deleteById(storyId) // Call the DAO method to delete the story
                Toast.makeText(requireContext(), "이야기가 성공적으로 삭제되었습니다!", Toast.LENGTH_SHORT).show()

                // Navigate back to the previous screen
                navController.popBackStack()

                // Optionally, you can use the following method if you need to ensure the previous fragment updates
                navController.previousBackStackEntry?.savedStateHandle?.set("refreshList", true)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "이야기 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBookmarkIcon() {
        val bookmarkIconResId = if (storyItem.isFavorite) {
            R.drawable.baseline_bookmark_24
        } else {
            R.drawable.baseline_bookmark_border_24
        }
        binding.bookmarkIcon.setImageResource(bookmarkIconResId)
    }

    private fun toggleFavoriteStatus() {
        storyItem = storyItem.copy(isFavorite = !storyItem.isFavorite)

        lifecycleScope.launch(Dispatchers.IO) { // IO 디스패처 사용
            updateStoryItemInDatabase(storyItem)

            withContext(Dispatchers.Main) { // UI 업데이트는 메인 스레드에서 실행
                updateBookmarkIcon()
            }
        }
    }

    private fun showBookmarkConfirmationDialog() {
        val message = if (storyItem.isFavorite) {
            "북마크를 해제하시겠습니까?"
        } else {
            "북마크를 설정하시겠습니까?"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("북마크 확인")
            .setMessage(message)
            .setPositiveButton("예") { _, _ -> toggleFavoriteStatus() }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop()                   // Crop image(Optional)
            .compress(1024)          // Compress image size between 0 to 1,000. Optional
            .maxResultSize(1080, 1080) // Max image resolution to crop(Optional)
            .galleryMimeTypes(arrayOf("image/png", "image/jpg", "image/jpeg")) // Array of Mime types to allow gallery images
            .start()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            val uri = data?.data
            if (uri != null) {
                binding.storyImage.setImageURI(uri) // 가져온 이미지를 imageView에 설정
                updateStoryItemInDatabase(storyItem.copy(storyImagePath = uri.toString()))
            }
        }
    }


    private fun updateStoryItemInDatabase(storyItem: StoryItem) {
        lifecycleScope.launch(Dispatchers.IO) { // IO 디스패처를 사용해 백그라운드에서 실행
            val storyEntity = storyItemToEntity(storyItem)
            try {
                storyDao.update(storyEntity) // 데이터베이스 업데이트
                Log.d("GalleryStoryDetailedFragment", "Update successful: $storyEntity")
            } catch (e: Exception) {
                Log.e("GalleryStoryDetailedFragment", "Error updating StoryEntity", e)
            }
        }
    }

    private fun storyItemToEntity(storyItem: StoryItem): StoryEntity {
        return StoryEntity(
            id = storyItem.id,
            storyTitle = storyItem.storyTitle,
            storyText = storyItem.storyText,
            storyImagePath = storyItem.storyImagePath,
            createdDate = storyItem.createdDate,
            isFavorite = storyItem.isFavorite // Add this line if it's part of your StoryEntity
        )
    }

    // Function to share the story image
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
                requireContext(),
                "${context?.packageName}.fileprovider",
                imageFile
            )
        }

        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context?.startActivity(Intent.createChooser(shareIntent, "이미지 공유하기"))

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
        context?.startActivity(Intent.createChooser(shareIntent, "스토리 공유하기"))
    }
}
