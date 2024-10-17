package com.cbnu.cat_ch.story.model

private const val DEFAULT = 512
private val sizes = listOf(
    512,
    640,
    720,
    768,
    1024,
)

class Sizes {
    companion object {
        fun getSizeList(): List<Int> {
            return sizes
        }

        fun getDefaultSize(): Int {
            return DEFAULT
        }
    }
}