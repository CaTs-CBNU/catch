package com.cbnu.cat_ch.gallery.fragments

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentGalleryStoriesBinding
import com.cbnu.cat_ch.gallery.adapter.GalleryAdapter
import com.cbnu.cat_ch.gallery.data.StoryItem
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.gallery.util.PdfUtils
import com.cbnu.cat_ch.gallery.viewmodel.GalleryViewModel
import com.cbnu.cat_ch.story.room.dao.StoryDao
import com.cbnu.cat_ch.story.room.db.StoryDatabase
import com.cbnu.cat_ch.story.room.entity.StoryEntity
import com.example.awesomedialog.AwesomeDialog
import com.example.awesomedialog.body
import com.example.awesomedialog.icon
import com.example.awesomedialog.onNegative
import com.example.awesomedialog.onPositive
import com.example.awesomedialog.title
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryStoriesFragment : Fragment() ,  GalleryAdapter.ActionModeCallback{
    private lateinit var binding: FragmentGalleryStoriesBinding
    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var navController: NavController
    private var actionMode: ActionMode? = null
    private var selectedItems: MutableList<StoryItem> = mutableListOf()
    private lateinit var storyDao: StoryDao

    // ActionMode callback for multi-selection
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.multi_select_menu, menu)
            mode?.customView?.background = ColorDrawable(ContextCompat.getColor(requireContext(), R.color.cat_ch))

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }


        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_delete -> {
                    // Logic when done button is clicked
                    deleteSelectedItems()
                    mode?.finish() // Close ActionMode
                    true
                }
                R.id.action_proceed -> {
                    // Logic when proceed button is clicked
                    proceedWithSelectedItems()
                    mode?.finish() // Close ActionMode
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            // Clear selection and end ActionMode
            galleryAdapter.clearSelection()
            actionMode = null
        }
    }

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val pdfFile = PdfUtils.uriToFile(requireContext(), it)
            pdfFile?.let { file ->
                navigateToPdfPreview(file)
            } ?: run {
                Toast.makeText(requireContext(), "파일을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), "PDF 파일을 선택하지 않았습니다", Toast.LENGTH_SHORT).show()
        }
    }

    private fun proceedWithSelectedItems() {
        val selectedItems = galleryAdapter.getSelectedItems()
        if (!validateStoryLengths(selectedItems)) {
            // Stop here if validation fails
            return
        }
        if (selectedItems.isNotEmpty()) {
            val bundle = Bundle().apply {
                putParcelableArrayList("story_items", ArrayList(selectedItems))
            }
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build()
            navController.navigate(
                R.id.action_galleryStoriesFragment_to_galleryPdfGeneratorFragment,
                bundle,
                navOptions
            )
        }
    }
    private fun validateStoryLengths(selectedItems: List<StoryItem>): Boolean {
        for (item in selectedItems) {
            if (item.storyTitle.length > 30) {
                // Show an alert if the title exceeds 30 characters
                AwesomeDialog.build(requireActivity())
                    .title("제목 글자수 초과", titleColor = R.color.black)
                    .body("이야기의 제목은 30자 이내여야 합니다: \"${item.storyTitle}\"")
                    .icon(R.drawable.baseline_info_outline)
                    .onPositive("확인") {}
                    .show()
                return false
            }
            if (item.storyText.length > 800) {
                // Show an alert if the story content exceeds 800 characters
                AwesomeDialog.build(requireActivity())
                    .title("내용 글자수 초과", titleColor = R.color.black)
                    .body("이야기 내용은 800자 이내여야 합니다: \"${item.storyTitle}\"")
                    .icon(R.drawable.baseline_info_outline)
                    .onPositive("확인") {}
                    .show()
                return false
            }
        }
        return true
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGalleryStoriesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        galleryViewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        storyDao = StoryDatabase.getDatabase(requireContext()).storyDao()

        val recyclerView = binding.recyclerView
        val shimmerViewContainer = binding.shimmerViewContainer
        val gridLayoutManager = GridLayoutManager(context, 2) // Two columns in the grid

        recyclerView.layoutManager = gridLayoutManager

        galleryAdapter = GalleryAdapter(requireContext(), emptyList(), { storyItem ->
            // Handle item click
        }, this)
        recyclerView.adapter = galleryAdapter

        // Start shimmer animation initially
        shimmerViewContainer.startShimmer()

        galleryAdapter.setOnItemLongClickListener {
            if (actionMode == null) {
                actionMode = requireActivity().startActionMode(actionModeCallback)
            }
            galleryAdapter.toggleSelection(it)
        }

        // Set up SpanSizeLookup for date headers
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (galleryAdapter.getItemViewType(position)) {
                    GalleryAdapter.VIEW_TYPE_DATE_HEADER -> gridLayoutManager.spanCount // Full width for date headers
                    else -> 1 // Regular grid item size
                }
            }
        }

        // Observe ViewModel for story data
        galleryViewModel.allStories.observe(viewLifecycleOwner) { stories ->
            shimmerViewContainer.stopShimmer() // Stop shimmer animation
            shimmerViewContainer.visibility = View.GONE // Hide shimmer

            if (stories.isEmpty()) {
                binding.noDataLayout.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                binding.noDataLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                val groupedStories = groupStoriesByDate(stories)
                galleryAdapter.updateData(groupedStories)
            }
        }
        // PDF 선택 FloatingActionButton 클릭 리스너
        binding.fabSelectPdf.setOnClickListener {
            selectPdfFile()
        }
        // Observe for refresh signal
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>("refreshList")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh) {
                    // Reload or refresh stories
                    galleryViewModel.refreshStories()
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "refreshList",
                        false
                    )
                }
            }
    }

    private fun groupStoriesByDate(stories: List<StoryEntity>): List<Any> {
        val dateFormat = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREAN)
        val grouped = stories.groupBy { dateFormat.format(Date(it.createdDate)) }
        val result = mutableListOf<Any>()
        grouped.forEach { (date, storiesForDate) ->
            result.add(date)
            result.addAll(storiesForDate.map { story ->
                StoryItem(
                    id = story.id,
                    storyImagePath = story.storyImagePath,
                    createdDate = story.createdDate,
                    formattedDate = date,
                    isFavorite = story.isFavorite,
                    storyTitle = story.storyTitle,
                    storyText = story.storyText
                )
            })
        }
        return result
    }

    private fun handleSelectedItems() {
        val selectedItems = galleryAdapter.getSelectedItems()
        selectedItems.forEach {
            Log.d("SelectedItem", it.storyTitle)
        }
    }

    override fun onItemLongClicked(storyItem: StoryItem) {
        if (actionMode == null) {
            actionMode = requireActivity().startActionMode(actionModeCallback)
        }
        toggleSelection(storyItem)
    }
    private fun toggleSelection(storyItem: StoryItem) {
        if (selectedItems.contains(storyItem)) {
            selectedItems.remove(storyItem)
        } else {
            selectedItems.add(storyItem)
        }
        // Notify adapter to refresh the item selection overlay
        galleryAdapter.setSelectedItems(selectedItems)
        actionMode?.title = "${selectedItems.size} 개가 선택되었습니다."
    }


    private fun selectPdfFile() {
        pdfPickerLauncher.launch("application/pdf")
    }

    private fun navigateToPdfPreview(pdfFile: File) {
        val bundle = Bundle().apply {
            putString("pdf_file", pdfFile.absolutePath)
        }
        navController.navigate(R.id.action_galleryStoriesFragment_to_pdfPreviewFragment, bundle , NavigationUtil.defaultNavOptions)

    }
    private fun deleteSelectedItems() {
        AwesomeDialog.build(requireActivity())
            .title("삭제 확인", titleColor = R.color.black)
            .body("선택한 스토리를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            .icon(R.drawable.baseline_info_outline)
            .onPositive("삭제") {
                // Confirm delete action
                val selectedIds = selectedItems.map { it.id }
                lifecycleScope.launch {
                    storyDao.deleteStoriesByIds(selectedIds)
                    galleryViewModel.refreshStories()
                    Toast.makeText(requireContext(), "선택한 스토리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                // Clear selected items and exit ActionMode
                selectedItems.clear()
                actionMode?.finish()
            }
            .onNegative("취소") {
                // Dismiss the dialog and do nothing
                actionMode?.finish()
            }
            .show()
    }
}
