package com.cbnu.cat_ch.story

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.aceinteract.android.stepper.StepperNavListener
import com.aceinteract.android.stepper.StepperNavigationView
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.ActivityStoryCreationBinding
import com.cbnu.cat_ch.story.viewmodel.StoryViewModel

class StoryCreationActivity : AppCompatActivity(), StepperNavListener {

    private lateinit var storyViewModel: StoryViewModel
    private lateinit var binding: ActivityStoryCreationBinding
    private lateinit var stepper: StepperNavigationView
    private lateinit var stepperNavListener: StepperNavListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewModel()
        initWidgets()
        initNavHostFragment()
    }

    private fun initViewModel() {
        storyViewModel = ViewModelProvider(this)[StoryViewModel::class.java]
    }
    private fun initWidgets(){
        stepper = binding.stepper
        stepperNavListener = this
        storyViewModel.currentStep.observe(this) { progress ->
            stepperNavListener.onStepChanged(progress)
        }
    }
    private fun initNavHostFragment(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? NavHostFragment
        val navController = navHostFragment?.navController
            ?: throw IllegalStateException("NavHostFragment not found in layout")
    }

    override fun onCompleted() {
    }

    override fun onStepChanged(step: Int) {
        val previousStep = storyViewModel.previousStep.value ?: 0
        if (step > previousStep) {
            // 현재 스텝이 이전 스텝보다 크면 다음 스텝으로 이동
            stepper.goToNextStep()
        } else if (step < previousStep) {
            // 현재 스텝이 이전 스텝보다 작으면 이전 스텝으로 이동
            stepper.goToPreviousStep()
            storyViewModel.updatePreviousStep(step)
        }
    }
}