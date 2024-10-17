package com.cbnu.cat_ch.gallery

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.cbnu.cat_ch.R
import com.cbnu.cat_ch.databinding.ActivityStoryGalleryBinding

class StoryGalleryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryGalleryBinding
    private lateinit var navController: NavController

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up NavController to use GalleryStoriesFragment as the initial fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.galleryFragmentContainer) as? NavHostFragment
        navController = navHostFragment?.navController
            ?: throw IllegalStateException("NavHostFragment not found in layout")
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, proceed with your logic
            } else {
                // Permissions denied, handle accordingly
            }
        }
    }
}
