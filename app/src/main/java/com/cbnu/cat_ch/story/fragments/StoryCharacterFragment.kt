package com.cbnu.cat_ch.story.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.FragmentStoryCharacterBinding
import com.cbnu.cat_ch.gallery.util.NavigationUtil
import com.cbnu.cat_ch.story.adapter.CharacterRoleAdapter
import com.cbnu.cat_ch.story.data.CharacterRole
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType
import smartdevelop.ir.eram.showcaseviewlib.config.Gravity


class StoryCharacterFragment : Fragment() {
    private lateinit var binding: FragmentStoryCharacterBinding
    private lateinit var navController: NavController
    private lateinit var storyViewModel: StoryViewModel

    private var characterRoles = mutableListOf<CharacterRole>()
    private lateinit var adapter: CharacterRoleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStoryCharacterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        storyViewModel = ViewModelProvider(requireActivity())[StoryViewModel::class.java]
        storyViewModel.updateCurrentStep(2)
        characterRoles = (storyViewModel.characterRoles.value ?: mutableListOf()).toMutableList()
        // Set up RecyclerView
        adapter = CharacterRoleAdapter(requireContext(), characterRoles)
        binding.recyclerViewCharacterRoles.adapter = adapter
        binding.recyclerViewCharacterRoles.layoutManager = LinearLayoutManager(requireContext())

        // Add character on button click
        binding.btnAddCharacter.setOnClickListener {
            addCharacter()
        }
        binding.helpIcon.setOnClickListener {
            showGuidesSequentially()
        }
        binding.btnNext.setOnClickListener {
            // Save character roles to ViewModel before navigating
            navController.navigate(R.id.action_storyCharacterFragment_to_storyPlotFragment,null, NavigationUtil.defaultNavOptions)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.navigate(R.id.action_storyCharacterFragment_to_storyTopicFragment,null, NavigationUtil.defaultNavOptions)
            storyViewModel.updatePreviousStep(2)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private fun addCharacter() {
        val characterName = binding.etCharacterName.text.toString()
        val characterRole = binding.etCharacterRole.text.toString()

        if (characterName.isNotEmpty() && characterRole.isNotEmpty()) {
            characterRoles.add(CharacterRole(characterName, characterRole))
            storyViewModel.addCharacterRoles(characterRoles)
            adapter.notifyItemInserted(characterRoles.size - 1)  // Notify adapter of new item
            adapter.updateData(characterRoles)
            // Clear input fields
            binding.etCharacterName.text.clear()
            binding.etCharacterRole.text.clear()
        }
    }

    private fun showGuidesSequentially() {
        // Show the first GuideView
        val firstGuideView = GuideView.Builder(requireContext())
            .setTitle("안내")
            .setContentText("등장인물의 이름을 작성해주세요!")
            .setGravity(Gravity.valueOf("auto"))
            .setTargetView(binding.etCharacterName)
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
            .setTargetView(binding.etCharacterRole)
            .setContentText("해당 인물의 작중 역할을 작성해주세요!")
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
            .setTargetView(binding.btnAddCharacter)
            .setContentText("작성이 완료되었다면 버튼을 클릭해주세요!")
            .setDismissType(DismissType.outside)
            .setGuideListener {
                showFourthGuide()
            }
            .build()
        thirdGuideView.show()
    }

    private fun showFourthGuide() {
        // Show the second GuideView
        val fourthGuideView = GuideView.Builder(requireContext())
            .setTitle("안내")
            .setGravity(Gravity.center)
            .setTargetView(binding.btnNext)
            .setContentText("모든 등장인물을 추가했다면 버튼을 눌러 다음 단게로 이동해주세요!")
            .setDismissType(DismissType.outside)
            .setGuideListener {
//                showThirdGuide()
            }
            .build()
        fourthGuideView.show()
    }
}