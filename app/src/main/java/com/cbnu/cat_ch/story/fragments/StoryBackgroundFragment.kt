package com.cbnu.cat_ch.story.fragments

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryBackgroundBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.example.awesomedialog.AwesomeDialog
import com.example.awesomedialog.body
import com.example.awesomedialog.icon
import com.example.awesomedialog.onPositive
import com.example.awesomedialog.title
import com.skydoves.powerspinner.SpinnerAnimation
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity

class StoryBackgroundFragment : Fragment() {

    private lateinit var binding: FragmentStoryBackgroundBinding
    private lateinit var navController: NavController
    private lateinit var storyViewModel: StoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStoryBackgroundBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
        storyViewModel.updateCurrentStep(4)
        setupSpinnerViews()  // PowerSpinnerView 설정
        setupNextButton()  // 다음 버튼 설정
        // List of spinners to dismiss

    }

    private fun setupSpinnerViews() {
        val spinners = listOf(binding.etAtmosphere, binding.etLocation)

        // Set up dismiss listeners for each spinner to detect when they close
        spinners.forEach { spinner ->
            spinner.setOnSpinnerOutsideTouchListener { _, _ ->
                spinner.dismiss()
            }
        }



        // PowerSpinnerView에 아이템 선택 시 ViewModel에 업데이트
        binding.etAtmosphere.setOnSpinnerItemSelectedListener<String> { _, _, _, selectedItem ->
            storyViewModel.updateAtmosphere(selectedItem)
        }

        // 사용자 정의 입력을 위한 EditText
        binding.etAtmosphereCustom.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etAtmosphereCustom.text.isNotEmpty()) {
                storyViewModel.updateAtmosphere(binding.etAtmosphereCustom.text.toString())
            }
        }

        binding.etLocation.setOnSpinnerItemSelectedListener<String> { _, _, _, selectedItem ->
            storyViewModel.updateLocation(selectedItem)
        }

        binding.etLocationCustom.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etLocationCustom.text.isNotEmpty()) {
                storyViewModel.updateLocation(binding.etLocationCustom.text.toString())
            }
        }

        binding.etEmotionCustom.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etEmotionCustom.text.isNotEmpty()) {
                storyViewModel.updateEmotion(binding.etEmotionCustom.text.toString())
            }
        }

        binding.etEmotion.setOnSpinnerItemSelectedListener<String> { _, _, _, selectedItem ->
            storyViewModel.updateEmotion(selectedItem)
        }

        // 이전 저장된 데이터를 사용하여 스피너와 커스텀 입력 필드 초기화
        storyViewModel.atmosphere.observe(viewLifecycleOwner) { atmosphere ->
            if (atmosphere.isNotEmpty()) binding.etAtmosphereCustom.setText(atmosphere)
        }
        storyViewModel.location.observe(viewLifecycleOwner) { location ->
            if (location.isNotEmpty()) binding.etLocationCustom.setText(location)
        }
        storyViewModel.emotion.observe(viewLifecycleOwner) { emotion ->
            if (emotion.isNotEmpty()) binding.etEmotionCustom.setText(emotion)
        }
    }


    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            val atmosphere = if (binding.etAtmosphereCustom.text.isNotEmpty()) {
                binding.etAtmosphereCustom.text.toString()
            } else {
                binding.etAtmosphere.text.toString()
            }

            val location = if (binding.etLocationCustom.text.isNotEmpty()) {
                binding.etLocationCustom.text.toString()
            } else {
                binding.etLocation.text.toString()
            }

            val emotion = if (binding.etEmotionCustom.text.isNotEmpty()) {
                binding.etEmotionCustom.text.toString()
            } else {
                binding.etEmotion.text.toString()
            }
            if (atmosphere.isEmpty()) {
                showGuideForMissingAtmosphere()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                showGuideForMissingLocation()
                return@setOnClickListener
            }

            if (emotion.isEmpty()) {
                showGuideForMissingEmotion()
                return@setOnClickListener
            }

            storyViewModel.updateAtmosphere(atmosphere)
            storyViewModel.updateLocation(location)
            storyViewModel.updateEmotion(emotion)

            navController.navigate(
                R.id.action_storyBackgroundFragment_to_storyPreviewFragment,
                null,
                NavigationUtil.defaultNavOptions
            )
        }
    }

    // Guide for missing atmosphere
    private fun showGuideForMissingAtmosphere() {
        // Scroll to the atmosphere field if necessary
        binding.etAtmosphere.post {
            binding.etAtmosphere.requestRectangleOnScreen(
                Rect(0, 0, binding.llAtmosphere.width, binding.llAtmosphere.height),
                true
            )
        }
        binding.llAtmosphere.postDelayed({
            val advancedOptionsGuideView = GuideView.Builder(requireContext())
                .setTitle("필수 항목")
                .setContentText("분위기를 선택 또는 입력해 주세요.")
                .setGravity(Gravity.auto)
                .setTargetView(binding.llAtmosphere)
                .setDismissType(DismissType.outside)
                .build()
            advancedOptionsGuideView.show()
        }, 200)
    }

    // Guide for missing location
    private fun showGuideForMissingLocation() {
        // Scroll to the atmosphere field if necessary
        binding.etAtmosphere.post {
            binding.etAtmosphere.requestRectangleOnScreen(
                Rect(0, 0, binding.llLocation.width, binding.llLocation.height),
                true
            )
        }
        binding.llLocation.postDelayed({
            val advancedOptionsGuideView = GuideView.Builder(requireContext())
                .setTitle("필수 항목")
                .setContentText("장소를 선택 또는 입력해 주세요.")
                .setGravity(Gravity.auto)
                .setTargetView(binding.llLocation)
                .setDismissType(DismissType.outside)
                .build()
            advancedOptionsGuideView.show()
        }, 200)
    }

    // Guide for missing emotion
    private fun showGuideForMissingEmotion() {
        // Scroll to the atmosphere field if necessary
//        binding.llEmotion.post {
//            binding.etAtmosphere.requestRectangleOnScreen(
//                Rect(0, 0, binding.llEmotion.width, binding.llEmotion.height),
//                true
//            )
//        }
        binding.llEmotion.postDelayed({
            val advancedOptionsGuideView = GuideView.Builder(requireContext())
                .setTitle("필수 항목")
                .setContentText("감정을 선택 또는 입력해 주세요.")
                .setGravity(Gravity.auto)
                .setTargetView(binding.llEmotion)
                .setDismissType(DismissType.outside)
                .build()
            advancedOptionsGuideView.show()
        }, 200)
    }
}