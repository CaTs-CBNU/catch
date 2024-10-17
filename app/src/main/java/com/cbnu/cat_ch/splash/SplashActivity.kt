package com.cbnu.cat_ch.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cbnu.cat_ch.databinding.ActivitySplashBinding
import android.view.animation.AccelerateDecelerateInterpolator
import com.cbnu.cat_ch.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

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
                        // 페이드 아웃 후 MainActivity로 전환
                        fadeOutAndNavigateToMain()
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


    // MainActivity로 이동하는 함수
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 스플래시 액티비티 종료
    }
}
