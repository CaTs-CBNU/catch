package com.cbnu.cat_ch.story.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryTopicBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.example.awesomedialog.AwesomeDialog
import com.example.awesomedialog.body
import com.example.awesomedialog.icon
import com.example.awesomedialog.onNegative
import com.example.awesomedialog.onPositive
import com.example.awesomedialog.title
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity


class StoryTopicFragment : Fragment() {

    private lateinit var binding: FragmentStoryTopicBinding
    private lateinit var navController: NavController
    private lateinit var storyViewModel: StoryViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoryTopicBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
        storyViewModel.updateCurrentStep(1)
        // PowerSpinnerView 및 사용자 입력 필드 설정
        setupSpinnerViews()

        binding.btnNext.setOnClickListener {
            val customTopic = binding.etCustomTopic.text.toString().trim()
            val selectedTopic = binding.spinnerTopic.text.toString()

            // Custom topic input takes priority if filled
            if (customTopic.isNotEmpty()) {
                storyViewModel.setStoryTopic(customTopic)
            } else if (selectedTopic.isNotEmpty()) {
                storyViewModel.setStoryTopic(selectedTopic)
            } else {
                // Prompt the user if neither is selected
                AwesomeDialog.build(requireActivity())
                    .title("알림", titleColor = R.color.black)
                    .body("주제를 선택하거나 입력해 주세요.")
                    .icon(R.drawable.baseline_info_outline)
                    .onPositive("확인")
                    .show()
                return@setOnClickListener
            }
            navController.navigate(
                R.id.action_storyTopicFragment_to_storyCharacterFragment,
                null,
                NavigationUtil.defaultNavOptions
            )
        }
        binding.ivHelp.setOnClickListener {
            showGuidesSequentially()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showExitConfirmationDialog()
        }
    }
    private fun setupSpinnerViews() {
        binding.spinnerTopic.setOnSpinnerOutsideTouchListener { _, _ ->
            binding.spinnerTopic.dismiss()
        }
        // PowerSpinnerView에 아이템 선택 시 ViewModel에 업데이트
        binding.spinnerTopic.setOnSpinnerItemSelectedListener<String> { _, _, _, selectedItem ->
            storyViewModel.setStoryTopic(selectedItem)
        }

        // 사용자 정의 입력을 위한 EditText
        binding.etCustomTopic.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && binding.etCustomTopic.text.isNotEmpty()) {
                storyViewModel.setStoryTopic(binding.etCustomTopic.text.toString())
            }
        }

        // 이전 저장된 데이터를 사용하여 스피너와 커스텀 입력 필드 초기화
        storyViewModel.storyTopic.observe(viewLifecycleOwner) { topic ->
            if (topic.isNotEmpty()) {
                val topicsArray = requireContext().resources.getStringArray(R.array.story_topics)

                // Spinner 아이템에 존재하는 경우 Spinner에서 선택
                if (topic in topicsArray) {
                    if (binding.spinnerTopic.text.toString() != topic) {
                        binding.spinnerTopic.selectItemByIndex(topicsArray.indexOf(topic))
                    }
                } else {
                    // 사용자 정의 텍스트 입력에 기존 값이 없는 경우에만 설정
                    if (binding.etCustomTopic.text.toString() != topic) {
                        binding.etCustomTopic.setText(topic)
                    }
                }
            }
        }

    }
    private fun showExitConfirmationDialog() {
        AwesomeDialog.build(requireActivity())
            .title("정말이세요?", titleColor = R.color.black)
            .body("이야기 생성 중단시 작성된 내용은 저장되지 않습니다.")
            .icon(R.drawable.baseline_info_outline)
            .onPositive("네") {
                Log.d("TAG", "positive ")
                // Navigate to MainActivity
                requireActivity().finish()
            }
            .onNegative("아니요",) {
                Log.d("TAG", "negative ")
            }
    }

    private fun showGuidesSequentially() {
        // Show the first GuideView
        val firstGuideView = GuideView.Builder(requireContext())
            .setTitle("안내")
            .setContentText("만들고 싶은 장르를 선택해주세요!")
            .setGravity(Gravity.valueOf("auto"))
            .setTargetView(binding.spinnerTopic)
            .setDismissType(DismissType.outside)
            .setGuideListener {
                showSecondGuide()
            }
            .build()

        firstGuideView.show()
    }
    private fun showSecondGuide() {
        // Show the second GuideView
        val secondGuideView = GuideView.Builder(requireContext())
            .setTitle("안내")
            .setGravity(Gravity.center)
            .setTargetView(binding.etCustomTopic)
            .setContentText("만약 마음에 드는 장르가 없다면 직접 입력해주세요!\n본인이 직접 입력한 장르가 우선선택됩니다!")
            .setDismissType(DismissType.outside)
            .setGuideListener {
                showThirdGuide()
            }
            .build()
        secondGuideView.show()
    }
    private fun showThirdGuide() {
        // Show the second GuideView
        val thirdGuideView = GuideView.Builder(requireContext())
            .setTitle("안내")
            .setGravity(Gravity.center)
            .setTargetView(binding.btnNext)
            .setContentText("선택이 완료되었다면 다음 버튼을 클릭해주세요!")
            .setDismissType(DismissType.outside)
            .setGuideListener {
//                showThirdGuide()
            }
            .build()
        thirdGuideView.show()
    }
}