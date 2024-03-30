package com.example.silversimon_projetindiv

import android.graphics.Bitmap
import android.net.Uri


/**
 * Represents a photo stored in the application's internal memory.
 *
 *  @property name The name of the photo.
 *  @property bmp The image of the photo (bitmap)
 *  @property namePatient The patient's first name associated with the photo.
 *  @property gender The gender of the patient associated with the photo.
 *  @property coff The coefficient associated with the photo.
 *
 *  @author Thibaut Lesage
 *  @version 1.4 ( First only name and bmp, then added namePatient, then gender and finally coff)
 *
 *  Bitmap is a format that allows you to store images in memory. It is a matrix of pixels that represents an image
 *  and can be interpreted by every device.
 */
data class InternalStoragePhoto(
    val name: String,
    val bmp: Bitmap,
    val namePatient : String,
    val gender : String,
    val coff : Int
)