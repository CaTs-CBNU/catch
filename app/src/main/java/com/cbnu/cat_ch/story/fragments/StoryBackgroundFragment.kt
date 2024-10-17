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

        // 이전 저장된 데이터를 사용하여 스피너와 커스텀 입력 필드 초기화
        storyViewModel.atmosphere.observe(viewLifecycleOwner) { atmosphere ->
            if (atmosphere.isNotEmpty()) binding.etAtmosphereCustom.setText(atmosphere)
        }
        storyViewModel.location.observe(viewLifecycleOwner) { location ->
            if (location.isNotEmpty()) binding.etLocationCustom.setText(location)
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

            // Check if atmosphere is filled
            if (atmosphere.isEmpty()) {
                AwesomeDialog.build(requireActivity())
                    .title("알림", titleColor = R.color.black)
                    .body("분위기를 입력해 주세요.")
                    .icon(R.drawable.baseline_info_outline)
                    .onPositive("확인",)
                    .show()
                return@setOnClickListener
            }

            // Check if location is filled
            if (location.isEmpty()) {
                AwesomeDialog.build(requireActivity())
                    .title("알림", titleColor = R.color.black)
                    .body("장소를 입력해 주세요.")
                    .icon(R.drawable.baseline_info_outline)
                    .onPositive("확인")
                    .show()
                return@setOnClickListener
            }


            storyViewModel.updateAtmosphere(atmosphere)
            storyViewModel.updateLocation(location)

            navController.navigate(
                R.id.action_storyBackgroundFragment_to_storyPreviewFragment,
                null,
                NavigationUtil.defaultNavOptions
            )
        }
    }
}