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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class StoryPreviewFragment : Fragment() {
    val handler = Handler(Looper.getMainLooper())
    private lateinit var navController: NavController
    private lateinit var binding: FragmentStoryPreviewBinding
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var generativeModel: GenerativeModel
    val scrollInterval = 100L  // 스크롤 체크 간격 (밀리초)

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
        // Set click listener on the generate story button
        binding.btnGenerateStory.setOnClickListener {
            generateStory()
        }

        binding.btnNext.setOnClickListener {
            if (binding.tvStoryPreview.text.isEmpty()) {
                Toast.makeText(requireContext(), "이야기를 먼저 생성해 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            storyViewModel.setGenerationStoryPlot(binding.tvStoryPreview.text.toString())
            navController.navigate(R.id.action_storyPreviewFragment_to_storyImageGenerationFragment, null, NavigationUtil.defaultNavOptions)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            storyViewModel.updatePreviousStep(5)
            navController.navigate(R.id.action_storyPreviewFragment_to_storyBackgroundFragment,null, NavigationUtil.defaultNavOptions)

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

        // Create the prompt
        val prompt = "나는 주제가 ${topic}인  이야기를 만드려고 해, 등장인물은 ${characterDescriptions}이야, 배경은 ${storyViewModel.atmosphere.value ?: ""}, " +
                "줄거리는 ${plot}이야" +
                "장소는 ${storyViewModel.location.value ?: ""}이야. 700자 이내로 이야기를 만들어 줘!"

        // Show the ProgressBar
        binding.pbLoading.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        // Launch a coroutine to call the suspend function
        lifecycleScope.launch {
            var retries = 0
            val maxRetries = 3

            while (retries < maxRetries) {
                try {
                    // Prepare input content
                    val inputContent = content {
                        text(prompt)
                    }

                    // Call the API and get the response
                    val response = generativeModel.generateContent(inputContent)

                    // Update the TextView with the API response
                    storyViewModel.setGenerationStoryPlot(response. text.toString())
                    binding.pbLoading.visibility = View.INVISIBLE
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    binding.tvStoryPreview.setCharacterDelay(50)
                    binding.tvStoryPreview.animateText(response.text)
                    scrollWithAnimation()
                    binding.svStoryPreview.visibility = View.VISIBLE



                    break // 성공하면 루프를 벗어나기

                } catch (e: Exception) {
                    e.printStackTrace()

                    // Handling 429 Resource Exhausted error
                    if (e.message?.contains("429") == true) {
                        Toast.makeText(
                            requireContext(),
                            "API 사용 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                        retries++
                        delay(5000) // 5초 대기 후 재시도
                    } else {
                        Toast.makeText(requireContext(), "오류가 발생했습니다: ${e.message}", Toast.LENGTH_LONG).show()
                        binding.tvStoryPreview.text = "Error: ${e.message}"
                        break
                    }
                } finally {
                    // Hide the ProgressBar if we've exhausted retries
                    if (retries == maxRetries) {
                        binding.pbLoading.visibility = View.GONE
                        Toast.makeText(
                            requireContext(),
                            "API 요청이 여러 번 실패했습니다. 나중에 다시 시도해 주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
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

