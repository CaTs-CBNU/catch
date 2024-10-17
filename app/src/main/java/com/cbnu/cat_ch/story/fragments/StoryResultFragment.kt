package com.cbnu.cat_ch.story.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.cbnu.cat_ch.MainActivity
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryResultBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.room.db.StoryDatabase
import com.cbnu.cat_ch.story.room.entity.StoryEntity
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class StoryResultFragment : Fragment() {

    private lateinit var binding: FragmentStoryResultBinding
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoryResultBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        // Set the story plot text and generated image
        storyViewModel.generationStoryPlot.observe(viewLifecycleOwner) { plot ->
            binding.generatedStoryText.text = plot
        }

        storyViewModel.imagePath.observe(viewLifecycleOwner) { imagePath ->
            if (imagePath != null) {
                // Assuming you have a method to load images from a URI or path
                binding.generatedImageView.setImageURI(imagePath.toUri()) // Adjust as necessary
            }
        }

        storyViewModel.updateCurrentStep(7)

        binding.saveButton.setOnClickListener {
            showSaveConfirmationDialog()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            storyViewModel.updatePreviousStep(7)

            navController.navigate(R.id.action_storyResultFragment_to_storyImageGenerationFragment, null, NavigationUtil.defaultNavOptions)
        }
    }

    private fun saveStoryDirectly(storyTitle: String) {
        val storyText = binding.generatedStoryText.text.toString()

        if (storyText.isNotEmpty()) {
            // Access the database and save the story
            val storyDao = StoryDatabase.getDatabase(requireContext()).storyDao()

            lifecycleScope.launch {
                val storyEntity = StoryEntity(
                    storyTitle = storyTitle,
                    storyText = storyText,
                    storyImagePath = storyViewModel.imagePath.value, // Use the image path from ViewModel
                    createdDate = System.currentTimeMillis(),
                    isFavorite = false
                )
                storyDao.insert(storyEntity)
                // Show a confirmation message or handle UI
                Toast.makeText(requireContext(), "이야기를 성공적으로 저장했습니다!", Toast.LENGTH_SHORT).show()
                Log.d("StoryResultFragment", "Story saved successfully")

//                // Navigate back to the main activity
//                val navOptions = NavOptions.Builder()
//                    .setPopUpTo(R.id.nav_graph, inclusive = true) // nav_graph의 시작 지점까지 popUp
//                    .setLaunchSingleTop(true) // 이미 스택에 있을 경우 새로 만들지 않음
//                    .setEnterAnim(R.anim.fade_in)
//                    .setExitAnim(R.anim.fade_out)
//                    .setPopEnterAnim(R.anim.fade_in)
//                    .setPopExitAnim(R.anim.fade_out)
//                    .build()
//
//                navController.navigate(R.id.action_storyResultFragment_to_mainActivity, null, navOptions)

                val intent = Intent(requireContext(), MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish() // 현재 액티비티 종료하여 스택 비움
            }
        } else {
            Log.d("StoryResultFragment", "Cannot save an empty story")
            Toast.makeText(requireContext(), "이야기를 저장하는데 실패했습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    //    // Show confirmation dialog before saving story
    private fun showSaveConfirmationDialog() {
        // Create an EditText for the dialog input
        val titleEditText = EditText(requireContext()).apply {
            hint = "이야기 제목"
            filters = arrayOf(InputFilter.LengthFilter(30)) // Limit to 30 characters
        }
        // Add TextWatcher to show a warning if 30 characters are exceeded
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length > 30) {
                    Toast.makeText(requireContext(), "제목은 최대 30자까지 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Create and show the dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("저장 확인")
            .setMessage("정말로 이 이야기를 저장하시겠습니까?")
            .setView(titleEditText) // Add the EditText to the dialog
            .setPositiveButton("저장") { _, _ ->
                // Get the title from the EditText
                val storyTitle = titleEditText.text.toString().trim()
                if (storyTitle.isNotEmpty()) {
                    saveStoryDirectly(storyTitle)
                } else {
                    Toast.makeText(requireContext(), "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
