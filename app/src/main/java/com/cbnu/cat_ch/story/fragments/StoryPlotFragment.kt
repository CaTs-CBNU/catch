package com.cbnu.cat_ch.story.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryPlotBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.adapter.CharacterRoleAdapter
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import com.example.awesomedialog.AwesomeDialog
import com.example.awesomedialog.body
import com.example.awesomedialog.icon
import com.example.awesomedialog.onPositive
import com.example.awesomedialog.title
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity

class StoryPlotFragment : Fragment() {

    private lateinit var binding: FragmentStoryPlotBinding
    private lateinit var navController: NavController
    private lateinit var storyViewModel: StoryViewModel
    private lateinit var characterRoleAdapter: CharacterRoleAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoryPlotBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
        storyViewModel.updateCurrentStep(3)
        setUI()
        buttonNext()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            storyViewModel.updatePreviousStep(3)
            navController.navigate(R.id.action_storyPlotFragment_to_storyCharacterFragment,null, NavigationUtil.defaultNavOptions)
        }
    }
    private fun setUI() {
        // Initialize RecyclerView
        binding.rvCharacterList.layoutManager = LinearLayoutManager(requireContext())
        characterRoleAdapter = CharacterRoleAdapter(requireContext(), listOf())
        binding.rvCharacterList.adapter = characterRoleAdapter
        // Observe ViewModel's character roles
        storyViewModel.characterRoles.observe(viewLifecycleOwner) { characterRoles ->
            characterRoleAdapter.updateData(characterRoles)
            if (characterRoles.isNullOrEmpty()) {
                // 등장인물이 없을 때 "등장인물 설정되지 않음!" 표시
                binding.tvNoCharacters.visibility = View.VISIBLE
                binding.rvCharacterList.visibility = View.GONE
            } else {
                // 등장인물이 있을 때 RecyclerView 표시
                binding.tvNoCharacters.visibility = View.GONE
                binding.rvCharacterList.visibility = View.VISIBLE
            }
        }
        binding.etPlotDescription.setText(storyViewModel.storyPlot.value)
        // 드롭다운 선택 시 EditText에 예시 내용 추가
        binding.spinnerPlotExample.setOnSpinnerItemSelectedListener<String> { _, _, _, item ->
            binding.etPlotDescription.setText(item)
        }

        binding.spinnerPlotExample.setOnSpinnerOutsideTouchListener { _, _ ->
            binding.spinnerPlotExample.dismiss()
        }
        binding.ivHelp.setOnClickListener {
            showGuidesSequentially()
        }
    }
    private fun buttonNext() {
        binding.btnNext.setOnClickListener {
            if (binding.etPlotDescription.text.toString().isEmpty()) {
                // Prompt the user if neither is selected
                AwesomeDialog.build(requireActivity())
                    .title("알림", titleColor = R.color.black)
                    .body("스토리를 선택하거나 입력해 주세요.")
                    .icon(R.drawable.baseline_info_outline)
                    .onPositive("확인")
                    .show()
                return@setOnClickListener
            } else {
                storyViewModel.setStoryPlot(binding.etPlotDescription.text.toString())
                navController.navigate(R.id.action_storyPlotFragment_to_storyBackgroundFragment,null, NavigationUtil.defaultNavOptions)
            }
        }
    }
    private fun showGuidesSequentially() {
        // Show the first GuideView
        val firstGuideView = GuideView.Builder(requireContext())
            .setTitle("안내")
            .setContentText("본인이 생각한 줄거리를 작성해주세요!")
            .setGravity(Gravity.valueOf("auto"))
            .setTargetView(binding.etPlotDescription)
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
            .setTargetView(binding.spinnerPlotExample)
            .setContentText("만약 줄거리 작성이 힘들다면 예시를 이용해보세요!")
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
            }
            .build()
        thirdGuideView.show()
    }
}