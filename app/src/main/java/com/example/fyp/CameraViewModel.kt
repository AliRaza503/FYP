package com.example.fyp

import android.util.Log
import androidx.lifecycle.ViewModel
import java.lang.NumberFormatException

class CameraViewModel : ViewModel() {
    companion object {
        var IMAGE_NAMES_LIST = mutableListOf(
            "1. Front",
            "2. Right",
            "3. Back",
            "4. Left",
            "5. Top-Right-Corner",
            "6. Top",
            "7. Top-Left-Corner",
            "8. Bottom-Left-Corner",
            "9. Bottom",
            "10. Bottom-Right-Corner",
            "11. Back-Camera-Close-Up",
            "12. Blank-White-Screen-Test"
        )
        val DRAWABLE_NAMES = listOf(
            (R.drawable.ic_1_front),
            (R.drawable.ic_2_right),
            (R.drawable.ic_3_back),
            (R.drawable.ic_4_left),
            (R.drawable.ic_5_top_right_corner),
            (R.drawable.ic_6_top),
            (R.drawable.ic_7_top_left_corner),
            (R.drawable.ic_8_bottom_left_corner),
            (R.drawable.ic_9_bottom),
            (R.drawable.ic_10_bottom_right_corner),
            (R.drawable.ic_11_camera_closeup),
            (R.drawable.ic_12_front_blank_white_screen)
        )
    }

    private val toBeCapturedImagesList = mutableListOf<String>().apply {
        addAll(IMAGE_NAMES_LIST)
    }
    private val addedIndices = mutableListOf<Boolean>().apply {
        addAll(List(IMAGE_NAMES_LIST.size) { false })
    }

    private fun getIndexOfFirstImgNotAddedYet(): Int {
        val index = addedIndices.indexOf(false)
        if (index == -1) {
            addedIndices.forEachIndexed { i, _ ->
                addedIndices[i] = false
            }
            return 0
        }
        return index
    }

    fun getImageName(): String = toBeCapturedImagesList[getIndexOfFirstImgNotAddedYet()]
    fun getDrawableName(): Int =
        DRAWABLE_NAMES[toBeCapturedImagesList[getIndexOfFirstImgNotAddedYet()].substringBeforeLast(".")
            .toInt() - 1]

    fun newImageAdded(imageName: String) {
        val name = try {
            imageName.substringBeforeLast("_") + "_" + (imageName.substringAfterLast("_")
                .toInt() + 1)
        } catch (e: NumberFormatException) {
            imageName + "_1"
        }
        val index = name.substringBefore(".").toInt() - 1
        toBeCapturedImagesList[index] = name
        addedIndices[index] = true
        getIndexOfFirstImgNotAddedYet()
        Log.d("CameraViewModel", "newImageAdded: $toBeCapturedImagesList")
        Log.d("CameraViewModel", "addedIndices: $addedIndices")
    }

    fun getDrawableIndex(): Int {
        return toBeCapturedImagesList[getIndexOfFirstImgNotAddedYet()].substringBeforeLast(".")
            .toInt() - 1
    }
}