package com.cbnu.cat_ch.story.fragments

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.cbnu.cat_ch.BuildConfig
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryPreviewBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.nitish.typewriterview.TypeWriterView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class StoryPreviewFragment : Fragment() {
    val handler = Handler(Looper.getMainLooper())
    private lateinit var navController: NavController
    private lateinit var binding: FragmentStoryPreviewBinding
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var generativeModel: GenerativeModel
    val scrollInterval = 100L  // 스크롤 체크 간격 (밀리초)
    private var isGeneratingStory = false // 이야기 생성 중 여부를 추적
    private var generateStoryJob: Job? = null // 코루틴 작업 추적

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoryPreviewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
        storyViewModel.updateCurrentStep(5)
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-pro-latest",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
        // '다음' 버튼을 비활성화 상태로 설정
        binding.btnNext.isEnabled = false
        // Set click listener on the generate story button
        binding.btnGenerateStory.setOnClickListener {
            generateStory()
        }

        binding.btnNext.setOnClickListener {
            if (binding.tvStoryPreview.text.isEmpty()) {
                Toast.makeText(requireContext(), "이야기를 먼저 생성해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
//            storyViewModel.setGenerationStoryPlot(binding.tvStoryPreview.text.toString())
            navController.navigate(R.id.action_storyPreviewFragment_to_storyImageGenerationFragment, null, NavigationUtil.defaultNavOptions)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isGeneratingStory) {
                // 이야기 생성 중일 때는 뒤로가기 비활성화
                Toast.makeText(requireContext(), "이야기 생성 중입니다. 완료 후 뒤로가기 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 이전 단계로 이동
                storyViewModel.updatePreviousStep(5)
                navController.navigate(R.id.action_storyPreviewFragment_to_storyBackgroundFragment, null, NavigationUtil.defaultNavOptions)
            }
        }
    }

    private fun generateStory() {
        val topic = storyViewModel.storyTopic.value ?: ""
        val plot = storyViewModel.storyPlot.value ?: ""
        val characters = storyViewModel.characterRoles.value ?: listOf()

        // Format character roles into a string
        val characterDescriptions = characters.joinToString(separator = ", ") {
            "${it.characterName} (${it.characterRole})"
        }

        val prompt = "나는 주제가 ${topic}인 이야기를 만들려고 해. " +
                "${if (characterDescriptions.isNotEmpty()) "등장인물은 ${characterDescriptions}이고," else "등장인물은 정해지지 않았지만,"} " +
                "이들은 ${storyViewModel.atmosphere.value ?: "특별한 분위기"} 속에서 " +
                "${storyViewModel.location.value ?: "특정 장소"}에서 이야기를 펼쳐나가. " +
                "줄거리는 ${plot}이며, 이 이야기는 ${storyViewModel.emotion.value ?: "감정적인 긴장감"}을 담고 있어. " +
                "500자 이내로 이 이야기의 핵심을 담아줘!"

        // Show the ProgressBar
        binding.pbLoading.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        // 이야기 생성 작업이 진행 중임을 표시
        isGeneratingStory = true

        // Launch a coroutine to call the suspend function
        generateStoryJob = lifecycleScope.launch {
            var retries = 0
            val maxRetries = 3

            while (retries < maxRetries) {
                try {
                    val inputContent = content { text(prompt) }
                    val response = generativeModel.generateContent(inputContent)
                    // 스토리 생성 성공 시 다음 버튼 활성화
                    binding.btnNext.isEnabled = true  // 성공하면 다음 버튼 활성화
                    storyViewModel.updateGenerationStoryPlot(response.text.toString())
                    binding.pbLoading.visibility = View.INVISIBLE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    binding.tvStoryPreview.setCharacterDelay(50)
                    binding.tvStoryPreview.animateText(response.text)
                    scrollWithAnimation()
                    binding.svStoryPreview.visibility = View.VISIBLE

                    break // 성공 시 루프 탈출

                } catch (e: Exception) {
                    e.printStackTrace()

                    if (e.message?.contains("429") == true) {
                        Toast.makeText(
                            requireContext(),
                            "API 사용 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                        retries++
                        delay(5000)
                    } else {
                        Toast.makeText(requireContext(), "오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                        binding.tvStoryPreview.text = "Error: ${e.message}"
                        break
                    }
                } finally {
                    if (retries == maxRetries) {
                        binding.pbLoading.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "API 요청이 여러 번 실패했습니다. 나중에 다시 시도해 주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    // ProgressBar를 숨기고 터치 기능 다시 활성화
                    binding.pbLoading.visibility = View.INVISIBLE
                    isGeneratingStory = false
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Fragment가 종료될 때 코루틴 작업 취소
        generateStoryJob?.cancel()
    }
    // 애니메이션이 진행 중일 때 ScrollView가 계속 스크롤을 따라가도록 설정
    fun scrollWithAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // 애니메이션이 진행 중이면 ScrollView를 맨 아래로 스크롤
                if (binding.tvStoryPreview.isAnimationRunning) {
                    binding.svStoryPreview.post {
                        binding.svStoryPreview.fullScroll(View.FOCUS_DOWN)
                    }
                    // 일정 시간마다 다시 호출
                    handler.postDelayed(this, scrollInterval)
                }
            }
        }, scrollInterval)
    }


}

