package com.cbnu.cat_ch.story.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.cbnu.cat_ch.BuildConfig
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryImageGenerationBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.data.PollinationsAiRepository
import com.cbnu.cat_ch.story.model.Models
import com.cbnu.cat_ch.story.model.Sizes
import com.cbnu.cat_ch.story.viewmodel.PollinatorViewModel
import com.cbnu.cat_ch.story.viewmodel.PollinatorViewModelFactory
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.skydoves.powerspinner.PowerSpinnerView
import kotlinx.coroutines.launch
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity


class StoryImageGenerationFragment : Fragment() {

    private lateinit var binding: FragmentStoryImageGenerationBinding
    private lateinit var pollinatorViewModel: PollinatorViewModel
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var navController: NavController
    private lateinit var advancedOptionsTextView: TextView
    private lateinit var advancedOptionsContainer: LinearLayout
    private var isExpanded = false
    private val models = Models.getModelList().map { it.displayName }
    private val sizes = Sizes.getSizeList()
    private val defaultSize = Sizes.getDefaultSize()
    private lateinit var widthSpinner: PowerSpinnerView
    private lateinit var heightSpinner: PowerSpinnerView
    private lateinit var modelSpinner: PowerSpinnerView
    private lateinit var genreSpinner: PowerSpinnerView
    private lateinit var compositionSpinner: PowerSpinnerView
    private lateinit var generativeModel: GenerativeModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStoryImageGenerationBinding.inflate(layoutInflater)

        val repository = PollinationsAiRepository() // Replace with the correct instantiation if needed
        pollinatorViewModel = ViewModelProvider(this, PollinatorViewModelFactory(repository))[PollinatorViewModel::class.java]
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
        // OtherViewModel을 PollinatorViewModel의 listener로 설정
        pollinatorViewModel.listener = storyViewModel
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-pro-latest",
            apiKey = BuildConfig.GEMINI_API_KEY)
        advancedOptionsTextView = view.findViewById(R.id.advancedOptionsTextView)
        advancedOptionsContainer = view.findViewById(R.id.advancedOptionsContainer)
        val storyPlot = storyViewModel.generationStoryPlot.value.toString()
        binding.promptEditText.text = Editable.Factory.getInstance().newEditable(storyPlot)
        binding.promptEditText.setOnTouchListener { v, event ->
            if (v.id == binding.promptEditText.id) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }

        storyViewModel.updateCurrentStep(6)
        setupSpinners()
        setupAdvancedOptionsToggle()
        setupObservers()

        binding.modelHelpIcon.setOnClickListener {
            showGuideForAdvancedOptionsModelSingle()
        }

        // Prompt endIcon에 클릭 이벤트 추가
        binding.promptInputLayout.setEndIconOnClickListener {
            showGuideForPrompt()
        }

        // Initialize GuideView for advanced options help
        binding.advancedOptionsHelpIcon.setOnClickListener {
            showGuideForAdvancedOptions()
        }
        binding.pollinateButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                generateImage()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            storyViewModel.updatePreviousStep(6)
            navController.navigate(R.id.action_storyImageGenerationFragment_to_storyPreviewFragment, null, NavigationUtil.defaultNavOptions)
        }
    }

    private fun setupSpinners() {
        // Convert integer sizes to strings
        val sizeStrings = sizes.map { it.toString() }

        widthSpinner = binding.widthDropdown
        heightSpinner = binding.heightDropdown
        modelSpinner = binding.modelDropdown
        genreSpinner = binding.genreDropdown  // Assuming these are added to XML
        compositionSpinner = binding.compositionDropdown

        widthSpinner.setItems(sizeStrings)
        heightSpinner.setItems(sizeStrings)
        modelSpinner.setItems(models)

        widthSpinner.selectItemByIndex(sizeStrings.indexOf(defaultSize.toString()))
        heightSpinner.selectItemByIndex(sizeStrings.indexOf(defaultSize.toString()))
        modelSpinner.selectItemByIndex(models.indexOf(Models.getDefaultModel().displayName))
    }

    private fun setupAdvancedOptionsToggle() {
        advancedOptionsTextView.setOnClickListener {
            if (isExpanded) {
                collapseAdvancedOptions()
            } else {
                expandAdvancedOptions()
            }
            isExpanded = !isExpanded
        }
    }

    private suspend fun generateImagePrompt(): String? {
        binding.pollinateButton.isEnabled = false
        binding.progressIndicatorPrompt.visibility = View.VISIBLE
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        val promptText = binding.promptEditText.text.toString()
        val imageGenre = genreSpinner.text.toString()
        val imageComposition = compositionSpinner.text.toString()

        // Format the full prompt
        val formattedPrompt = """
        $promptText
        Create an image in a **$imageGenre** style. The composition should show **$imageComposition**.
        Design the image to be visually captivating, with elements suitable for a dynamic storytelling experience.
        
        After translating the above story into English, write the prompt in sentences.
    """.trimIndent()

        return try {
            val inputContent = content {
                text(formattedPrompt)
            }
            val response = generativeModel.generateContent(inputContent)
            Log.d("StoryImageGenerationFragment", "Generated prompt: ${response.text}")
            binding.pollinateButton.isEnabled = true
            binding.progressIndicatorPrompt.visibility = View.GONE
            response.text // Return generated text
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "에러가 발생했습니다. ", Toast.LENGTH_SHORT).show()
            binding.pollinateButton.isEnabled = true
            binding.progressIndicatorPrompt.visibility = View.GONE
            e.printStackTrace()
            null // Return null in case of error
        }
    }

    private fun processGeneratedPrompt(generatedPrompt: String) {
        val width = sizes[widthSpinner.selectedIndex]
        val height = sizes[heightSpinner.selectedIndex]

        pollinatorViewModel.onPromptChanged(generatedPrompt)
        pollinatorViewModel.onWidthChanged(width)
        pollinatorViewModel.onHeightChanged(height)
        pollinatorViewModel.onModelChanged(Models.getModelList()[modelSpinner.selectedIndex])

        pollinatorViewModel.generateImage(requireContext())
    }
    private suspend fun generateImage() {
        // 프롬프트 생성 단계
        val generatedPrompt = generateImagePrompt()
        generatedPrompt?.let {
            // 모델 인풋 단계
            processGeneratedPrompt(it)
        }
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


    private fun expandAdvancedOptions() {
        advancedOptionsContainer.visibility = View.VISIBLE
        advancedOptionsContainer.alpha = 0f
        advancedOptionsContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
    }

    private fun collapseAdvancedOptions() {
        advancedOptionsContainer.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    advancedOptionsContainer.visibility = View.GONE
                }
            })
    }


    private fun setupObservers() {
        // Collect uiState from the ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            pollinatorViewModel.uiState.collect { state ->
                // Update visibility based on loading state and checkmark visibility
                binding.progressIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE

                // Handle error state
                if (state.isError) {
                    Toast.makeText(requireContext(), state.errorMessage ?: "Error occurred", Toast.LENGTH_SHORT).show()
                }

                // Check if the image generation is complete and successful
                if (!state.isLoading && !state.isError && state.showCheckmark) {
                    // Play the Lottie animation when checkmark is visible
                    binding.successAnimation.apply {
                        visibility = View.VISIBLE
                        // Start the animation
                        playAnimation()
                        // Add a listener to detect when the animation ends
                        addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {}

                            override fun onAnimationEnd(animation: Animator) {
                                // Navigate to the next fragment once the animation ends
//                                navController.navigate(R.id.action_storyImageGenerationFragment_to_storyResultFragment)
                                if (findNavController().currentDestination?.id == R.id.storyImageGenerationFragment) {
                                    findNavController().navigate(R.id.action_storyImageGenerationFragment_to_storyResultFragment,null, NavigationUtil.defaultNavOptions)
                                }
                            }


                            override fun onAnimationCancel(animation: Animator) {}

                            override fun onAnimationRepeat(animation: Animator) {}
                        })
                    }
                }
            }
        }
    }
    private fun showGuideForPrompt() {

        val promptGuideView = GuideView.Builder(requireContext())
            .setTitle("프롬프트 도움말")
            .setContentText("프롬프트에서 생성된 이야기를 수정할 수 있으며, \n위 프롬프트를 통해 이미지가 생성됩니다.")
            .setGravity(Gravity.auto)
            .setTargetView(binding.promptInputLayout)
            .setDismissType(DismissType.outside)
            .build()
        promptGuideView.show()
    }

    private fun showGuideForAdvancedOptions() {
        val advancedOptionsGuideView = GuideView.Builder(requireContext())
            .setTitle("고급 옵션 도움말")
            .setContentText("고급 옵션에서 이미지를 생성할 장르와 구도를 설정할 수 있습니다.")
            .setGravity(Gravity.auto)
            .setTargetView(binding.advancedOptionsTextView)
            .setDismissType(DismissType.outside)
            .setGuideListener {
                showGuideForAdvancedOptionsModel()
            }
            .build()
        advancedOptionsGuideView.show()
    }

    private fun showGuideForAdvancedOptionsModelSingle() {
        val advancedOptionsGuideView = GuideView.Builder(requireContext())
            .setTitle("고급 옵션 도움말")
            .setContentText( "- **모델 선택**: 이미지 생성 속도와 품질에 영향을 줍니다.\n" +
                    "   - Default: 속도와 품질의 균형이 좋습니다.\n" +
                    "   - Turbo: 빠른 속도로 간단한 이미지를 생성합니다.\n" +
                    "   - Flux: 느리지만 매우 디테일한 이미지를 생성합니다.\n\n" )
            .setGravity(Gravity.auto)
            .setTargetView(binding.modelDropdown)
            .setDismissType(DismissType.outside)
            .build()
        advancedOptionsGuideView.show()
    }
    private fun showGuideForAdvancedOptionsModel()  {
        // 고급 옵션 컨테이너 위치 조정
        if (!isExpanded) {
            expandAdvancedOptions()
            isExpanded = true

            binding.advancedOptionsContainer.post {
                binding.advancedOptionsContainer.requestRectangleOnScreen(
                    Rect(0, 0, binding.advancedOptionsContainer.width, binding.advancedOptionsContainer.height),
                    true
                )
            }
        }
        binding.advancedOptionsContainer.postDelayed({

            val advancedOptionsGuideView = GuideView.Builder(requireContext())
                .setTitle("고급 옵션 도움말")
                .setContentText( "- **모델 선택**: 이미지 생성 속도와 품질에 영향을 줍니다.\n" +
                        "   - Default: 속도와 품질의 균형이 좋습니다.\n" +
                        "   - Turbo: 빠른 속도로 간단한 이미지를 생성합니다.\n" +
                        "   - Flux: 느리지만 매우 디테일한 이미지를 생성합니다.\n\n" )
                .setGravity(Gravity.auto)
                .setTargetView(binding.modelDropdown)
                .setDismissType(DismissType.outside)
                .setGuideListener {
                    showGuideForAdvancedOptionsGenre()
                }
                .build()
            advancedOptionsGuideView.show()    }, 200)

    }

    private fun showGuideForAdvancedOptionsGenre()  {
        val advancedOptionsGuideView = GuideView.Builder(requireContext())
            .setTitle("고급 옵션 도움말")
            .setContentText("이미지가 어떤 장르나 구도로 생성될지 설정할 수 있습니다.")
            .setGravity(Gravity.auto)
            .setTargetView(binding.advancedOptionsContainer2)
            .setDismissType(DismissType.outside)
            .setGuideListener {
                showGuideForAdvancedOptionsSize()
            }
            .build()
        advancedOptionsGuideView.show()
    }

    private fun showGuideForAdvancedOptionsSize()  {
        // 레이아웃이 확장되고 나서 화면 중앙에 위치하도록 초점 요청
        binding.heightDropdown.post {
            binding.advancedOptionsContainer3.requestRectangleOnScreen(
                Rect(0, 0, binding.advancedOptionsContainer3.width, binding.advancedOptionsContainer3.height),
                true
            )
        }
        binding.advancedOptionsContainer3.postDelayed({    val advancedOptionsGuideView = GuideView.Builder(requireContext())
            .setTitle("고급 옵션 도움말")
            .setContentText("이미지의 크기를 설정할 수 있습니다.")
            .setGravity(Gravity.auto)
            .setTargetView(binding.advancedOptionsContainer3)
            .setDismissType(DismissType.outside)
            .setGuideListener {
            }
            .build()
            advancedOptionsGuideView.show()
                                                      }, 200)



    }
}