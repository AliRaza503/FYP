package com.example.fyp

import android.net.Uri

data class Photo(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val contentUri: Uri
)
