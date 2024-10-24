package com.cbnu.cat_ch.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cbnu.cat_ch.databinding.ActivitySplashBinding
import android.view.animation.AccelerateDecelerateInterpolator
import com.cbnu.cat_ch.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper()) // Main thread에서 실행되는 핸들러

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 애니메이션 시작
        startSplashAnimations()
    }

    private fun startSplashAnimations() {
        // 레이아웃은 나타내지만 내부 텍스트는 아직 보이지 않도록 설정
        binding.layoutSketch.alpha = 1f
        binding.textS.alpha = 0f
        binding.textCatch.alpha = 0f
        binding.textTch.alpha = 0f
        binding.textCats.text = ""  // CaTs 텍스트를 공백으로 시작

        // 첫 번째 텍스트: "자신만의 이야기를" 페이드 인
        binding.textIntro.animate()
            .alpha(1f)
            .setDuration(1000)
            .withEndAction {
                // "스", "캐", "치" 텍스트 그룹을 나타내기
                animateSketchText()
            }
            .start()
    }

    private fun animateSketchText() {
        // "스" 페이드 인 애니메이션
        binding.textS.animate()
            .alpha(1f)
            .setDuration(800)
            .withEndAction {
                // "캐"와 "치"가 위에서 떨어지는 애니메이션 시작
                dropAnimation(binding.textCatch)
                dropAnimation(binding.textTch, 100)

                // "해보세요" 페이드 인 애니메이션을 마지막에 추가
                binding.textEnd.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .setStartDelay(400) // "스캐치"가 완전히 나타난 후
                    .withEndAction {

                        // CaTs. 텍스트 애니메이션 시작
                        animateCatsText()
                    }
                    .start()
            }
            .start()
    }


    // 위에서 떨어지는 애니메이션
    private fun dropAnimation(view: View, delay: Long = 0) {
        view.alpha = 1f
        view.translationY = -200f
        view.animate()
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(delay)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    // 페이드 아웃 후 MainActivity로 이동하는 함수
    private fun fadeOutAndNavigateToMain() {
        binding.main.animate()
            .alpha(0f)
            .setDuration(1000) // 페이드 아웃 속도 조정
            .withEndAction {
                // MainActivity로 이동
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish() // 현재 스플래시 액티비티 종료
            }
            .start()
    }
    // "CaTs." 텍스트를 하나씩 시간차를 두고 나타내기 위한 함수
    private fun animateCatsText() {
        val text = "CaTs."
        var currentText = ""
        val delay: Long = 400 // 각 글자가 나타나는 시간 간격 (밀리초)
        binding.textCats.visibility = View.VISIBLE

        // 각 글자를 400ms 간격으로 추가
        for (i in text.indices) {
            handler.postDelayed({
                currentText += text[i]
                binding.textCats.text = currentText // 갱신된 텍스트 설정

                // 마지막 글자가 추가되었을 때 MainActivity로 전환
                if (i == text.length - 1) {
                    fadeOutAndNavigateToMain()
                }
            }, i * delay)
        }
    }

    // MainActivity로 이동하는 함수
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 스플래시 액티비티 종료
    }
}
