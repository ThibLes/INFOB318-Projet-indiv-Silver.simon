package com.example.silversimon_projetindiv

import android.graphics.Bitmap
import android.net.Uri


/**
 * Représente une photo stockée dans la mémoire interne de l'application.
 *
 * @property name Le nom de la photo.
 * @property bmp L'image de la photo.
 * @property namePatient Le prénom du patient associé à la photo.
 * @property gender Le genre du patient associé à la photo.
 * @property coff Le coefficient associé à la photo.
 */
data class InternalStoragePhoto(
    val name: String,
    val bmp: Bitmap,
    val namePatient : String,
    val gender : String,
    val coff : Int
)