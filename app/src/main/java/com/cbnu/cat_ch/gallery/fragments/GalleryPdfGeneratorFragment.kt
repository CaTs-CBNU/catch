package com.cbnu.cat_ch.gallery.fragments

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.gesture.GestureLibraries.fromFile
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.DialogPdfDetailsBinding
import com.cbnu.cat_ch.databinding.FragmentGalleryPdfGeneratorBinding
import com.cbnu.cat_ch.gallery.adapter.ItemMoveCallback
import com.cbnu.cat_ch.gallery.adapter.ThumbnailAdapter
import com.cbnu.cat_ch.gallery.data.StoryItem
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.gallery.util.PdfPreviewFragment
import com.cbnu.cat_ch.gallery.util.PdfUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ymg.pdf.viewer.PDFView
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class GalleryPdfGeneratorFragment : Fragment() {

    private lateinit var binding: FragmentGalleryPdfGeneratorBinding
    private lateinit var navController: NavController
    private lateinit var storyItems: List<StoryItem>
    private var currentItemIndex: Int = 0

    private lateinit var dateTimeText: TextView
    private lateinit var titleText: TextView
    private lateinit var storyImage: ImageView
    private lateinit var storyDescriptionText: TextView
    private lateinit var generatePdfButton: FloatingActionButton
    private lateinit var thumbnailRecyclerView: RecyclerView
    private lateinit var thumbnailAdapter: ThumbnailAdapter
    // Store file name and title temporarily for use after permission is granted
    private var pendingPdfFileName: String? = null
    private var pendingStoryTitle: String? = null

    // Request permission launcher, now using stored file name and title
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            pendingPdfFileName?.let { pdfFileName ->
                pendingStoryTitle?.let { storyTitle ->
                    downloadStory(storyItems, pdfFileName, storyTitle)
                }
            }
        } else {
            Toast.makeText(requireContext(), "권한 거부. 파일을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryPdfGeneratorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // Retrieve storyItems list or single storyItem from arguments
        storyItems = arguments?.getParcelableArrayList("story_items") ?: listOf(arguments?.getParcelable("story_item")!!)

        setupViews()
        setupThumbnailRecyclerView()
        displayStoryItem(currentItemIndex)
    }

    private fun setupViews() {
        dateTimeText = binding.dateTimeText
        titleText = binding.titleText
        storyImage = binding.storyImage
        storyDescriptionText = binding.storyDescriptionText
        generatePdfButton = binding.generatePdfButton

        generatePdfButton.setOnClickListener {
            showPdfDetailsDialog(titleText.text.toString())
        }
    }

    private fun setupThumbnailRecyclerView() {
        thumbnailRecyclerView = binding.thumbnailRecyclerView
        thumbnailAdapter = ThumbnailAdapter(storyItems) { index ->
            displayStoryItem(index)
        }
        thumbnailRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        thumbnailRecyclerView.adapter = thumbnailAdapter

        // Add ItemTouchHelper for drag-and-drop functionality
        val itemTouchHelper = ItemTouchHelper(ItemMoveCallback(thumbnailAdapter))
        itemTouchHelper.attachToRecyclerView(thumbnailRecyclerView)
    }

    private fun displayStoryItem(index: Int) {
        val storyItem = storyItems[index]

        // Update the main story item details
        dateTimeText.text = storyItem.formattedDate
        titleText.text = storyItem.storyTitle
        storyDescriptionText.text = storyItem.storyText

        // Set the main story image with animation
        storyItem.storyImagePath?.let {
            storyImage.setImageURI(it.toUri())

            // Scale animation on story image
            ObjectAnimator.ofFloat(storyImage, "scaleX", 0.9f, 1f).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            ObjectAnimator.ofFloat(storyImage, "scaleY", 0.9f, 1f).apply {
                duration = 300
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        // Update the selected thumbnail index
        currentItemIndex = index
        thumbnailAdapter.setSelectedIndex(index) // Update selected thumbnail in RecyclerView
    }


    private fun checkAndRequestPermission(pdfFileName: String, storyTitle: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API level 29) and above use Scoped Storage, no need to request WRITE_EXTERNAL_STORAGE
            downloadStory(storyItems, pdfFileName, storyTitle)
        } else {
            // For Android versions below Q
            // Check if WRITE_EXTERNAL_STORAGE permission is already granted
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted, proceed with download
                downloadStory(storyItems, pdfFileName, storyTitle)
            } else {
                // Request the permission
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun downloadStory(storyItems: List<StoryItem>, pdfFileName: String, storyTitle: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            lifecycleScope.launch {
                try {
                    val pdfFile = PdfUtils.generatePdfFile(storyItems, pdfFileName, storyTitle, requireContext())

                    if (pdfFile != null) {
                        val pdfUri = savePdfToMediaStore(pdfFile)

                        if (pdfUri != null) {
                            Toast.makeText(requireContext(), "PDF 다운로드 완료", Toast.LENGTH_LONG).show()
//                            showPdfPreviewDialog(pdfUri) // Pass URI instead of File
                            displayPdfPreview(pdfFile)
                        } else {
                            Toast.makeText(requireContext(), "PDF 저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "PDF 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("GalleryStoryDetailedFragment", "Error downloading story as PDF", e)
                    Toast.makeText(requireContext(), "PDF 다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                lifecycleScope.launch {
                    try {
                        val pdfFile = PdfUtils.generatePdfFile(storyItems, pdfFileName, storyTitle, requireContext())

                        if (pdfFile != null) {
                            val savedFile = savePdfToExternalStorage(pdfFile)

                            if (savedFile != null) {
                                Toast.makeText(requireContext(), "PDF 다운로드 완료: ${savedFile.absolutePath}", Toast.LENGTH_LONG).show()
                                displayPdfPreview(pdfFile)
//                                showSharePdfDialog(pdfFile)
                                if (savedFile.exists()) {
//                                    showPdfPreviewDialog(savedFile.toUri())
                                } else {
                                    Toast.makeText(requireContext(), "파일이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "PDF 저장 실패", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "PDF 생성 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("GalleryStoryDetailedFragment", "Error downloading story as PDF", e)
                        Toast.makeText(requireContext(), "PDF 다운로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun savePdfToExternalStorage(pdfFile: File): File? {
        val outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val outputFile = File(outputDir, pdfFile.name)

        return try {
            pdfFile.copyTo(outputFile, overwrite = true)
            outputFile
        } catch (e: IOException) {
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun savePdfToMediaStore(pdfFile: File): Uri? {
        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, pdfFile.name)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(uri)?.use { outputStream ->
                pdfFile.inputStream().copyTo(outputStream)
            }
            return uri // Return the Uri instead of File
        }

        return null
    }
    private fun showPdfDetailsDialog(storyTitle: String) {
        val dialogBinding = DialogPdfDetailsBinding.inflate(LayoutInflater.from(requireContext()))

        // Set the default text for storyTitleInput
        dialogBinding.storyTitleInput.setText(storyTitle)

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("PDF 파일 정보 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("생성") { _, _ ->
                val pdfFileName = dialogBinding.pdfNameInput.text.toString().ifBlank { "Generated_Stories" }
                val finalStoryTitle = dialogBinding.storyTitleInput.text.toString().ifBlank { "Story Collection" }
                checkAndRequestPermission(pdfFileName, finalStoryTitle)
            }
            .setNegativeButton("취소", null)
            .create()

        alertDialog.show()
    }

    private fun displayPdfPreview(pdfFile: File) {
        val args = Bundle().apply {
            putString("pdf_file", pdfFile.absolutePath) // String 경로로 전달
        }
        navController.navigate(R.id.action_galleryPdfGeneratorFragment_to_pdfPreviewFragment, args,NavigationUtil.defaultNavOptions)
    }
//
//    // PDF 공유 다이얼로그 표시
//    private fun showSharePdfDialog(pdfFile: File) {
//        val dialog = AlertDialog.Builder(requireContext())
//            .setTitle("PDF 공유")
//            .setMessage("생성된 PDF를 공유하시겠습니까?")
//            .setPositiveButton("공유") { _, _ ->
//                sharePdf(pdfFile)
//            }
//            .setNegativeButton("취소", null)
//            .create()
//
//        dialog.show()
//    }
//
//    // PDF 공유 메소드
//    private fun sharePdf(pdfFile: File) {
//        val pdfUri = FileProvider.getUriForFile(
//            requireContext(),
//            "${requireContext().packageName}.fileprovider",
//            pdfFile
//        )
//
//        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//            type = "application/pdf"
//            putExtra(Intent.EXTRA_STREAM, pdfUri)
//            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        }
//
//        startActivity(Intent.createChooser(shareIntent, "PDF 파일 공유"))
//    }



}
