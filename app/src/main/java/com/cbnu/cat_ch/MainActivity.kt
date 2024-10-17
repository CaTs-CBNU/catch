package com.cbnu.cat_ch

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cbnu.cat_ch.databinding.ActivityMainBinding
import com.cbnu.cat_ch.story.StoryCreationActivity
import com.cbnu.cat_ch.gallery.StoryGalleryActivity
import com.example.awesomedialog.AwesomeDialog
import com.example.awesomedialog.body
import com.example.awesomedialog.icon
import com.example.awesomedialog.onNegative
import com.example.awesomedialog.onPositive
import com.example.awesomedialog.title
import com.google.android.gms.oss.licenses.OssLicensesActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.permissionx.guolindev.PermissionX
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        showExitConfirmationDialog() // 종료 확인 대화상자 호출
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.developerInfoIcon.setOnClickListener {
            showDeveloperInfoDialog()
        }

        binding.btnGenerateStory.setOnClickListener {
            checkPermissionsAndProceed {
                proceedToStoryCreation()
            }
        }

        binding.btnViewStories.setOnClickListener {
            checkPermissionsAndProceed {
                proceedToStoryGallery()
            }
        }

        val texts = arrayOf("남들과는 다른", "자신만의 동화를 만들어보세요")
        val fadingTextView = binding.fadingTextView
        fadingTextView.setTexts(texts)
        fadingTextView.setTimeout(2400.milliseconds)
        binding.linkTextView.setOnClickListener {
            openWebsite("https://cats-cbnu.github.io/")
        }
    }
    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        startActivity(intent)
    }

    private fun proceedToStoryCreation() {
        if (isNetworkConnected()) {
            val intent = Intent(this, StoryCreationActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            // 애니메이션 설정
        } else {
            Toast.makeText(this, "네트워크가 연결되지 않았습니다, 인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun proceedToStoryGallery() {
        val intent = Intent(this, StoryGalleryActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun checkPermissionsAndProceed(action: () -> Unit) {
        // 권한 목록 설정
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 권한 요청 실행
        PermissionX.init(this)
            .permissions(permissions)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "이 기능을 사용하려면 저장소 접근 권한이 필요합니다.",
                    "확인",
                    "취소"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "권한 설정을 통해 저장소 접근 권한을 허용해 주세요.",
                    "설정",
                    "취소"
                )
            }
            .request { allGranted, _, _ ->
                if (allGranted) {
                    action()
                } else {
                    Toast.makeText(this, "스토리지 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showExitConfirmationDialog() {
        AwesomeDialog.build(this)
            .title("캐치를 종료하시겠습니까?", titleColor = R.color.black)
            .body("한번 더 생각해주세요.")
            .icon(R.drawable.baseline_info_outline)
            .onPositive("네") {
                Log.d("TAG", "positive ")
                // Navigate to MainActivity
                this.finish()
            }
            .onNegative("아니요") {
                Log.d("TAG", "negative ")
            }
    }
    private fun showDeveloperInfoDialog() {
        val appVersion = BuildConfig.VERSION_NAME
        val email = "taewangim05@gmail.com"
        val developerName = "Kim Tae Wan"
        val githubLink = "https://github.com/kimtaewan22"
        val license = "MIT License"
        val developmentTools = "Android Studio"
        val programmingLanguage = "Kotlin"

        val message = """
        이름: $developerName
        이메일: $email
        GitHub: $githubLink

        앱 버전: $appVersion
        개발 도구: $developmentTools
        개발 언어: $programmingLanguage
        오픈소스 라이선스: $license
    """.trimIndent()

        // Use MaterialAlertDialogBuilder for Material-styled dialog
        MaterialAlertDialogBuilder(this)
            .setTitle("개발자 정보")
            .setMessage(message)
            .setNeutralButton("오픈소스 라이선스") { _, _ ->
                // Launch OSS licenses activity
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
            .setPositiveButton("문의/오류제보") { _, _ ->
                // Send email intent for reporting issues
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                    putExtra(Intent.EXTRA_SUBJECT, "오류 제보 및 문의")
                }
                startActivity(Intent.createChooser(intent, "이메일 보내기"))
            }
            .setNegativeButton("닫기") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}
