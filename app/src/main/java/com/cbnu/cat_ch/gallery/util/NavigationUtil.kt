package com.cbnu.cat_ch.gallery.util

import androidx.navigation.NavOptions
import com.cbnu.cat_ch.R

object NavigationUtil {
    val defaultNavOptions: NavOptions by lazy {
        NavOptions.Builder()
            .setEnterAnim(R.anim.fade_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.fade_out)
            .build()
    }
}
