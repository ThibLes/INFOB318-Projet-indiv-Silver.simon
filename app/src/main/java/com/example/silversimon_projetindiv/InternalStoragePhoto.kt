package com.example.silversimon_projetindiv

import android.graphics.Bitmap
import android.net.Uri

data class InternalStoragePhoto(
    val name: String,
    val bmp: Bitmap,
    val namePatient : String,
    val gender : String
)