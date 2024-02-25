package com.example.silversimon_projetindiv

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.silversimon_projetindiv.databinding.ActivityGalleryBinding
import com.example.silversimon_projetindiv.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/*
 Utilisation de Stockage Interne --> Aucune autre appli ne pourra acceder aux photos de mon appli
 mais att si app supprimée --> les photos le seront aussi

 Avantage --> besoin de aucune permission
 */
class Gallery : AppCompatActivity(){

    private lateinit var binding: ActivityGalleryBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter

    // Ajoutez une variable pour le résultat de la prise de photo
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // Vérifiez si nous avons reçu un bitmap non-null
        bitmap?.let { nonNullBitmap ->
            // Générez un nom de fichier unique pour la photo
            val filename = UUID.randomUUID().toString()
            // Essayez d'enregistrer la photo dans le stockage interne
            val isSavedSuccessfully = savePhotoToInternalStorage(filename, nonNullBitmap)
            if(isSavedSuccessfully) {
                // Si la sauvegarde a réussi, rechargez les photos et affichez un message
                loadPhotosFromInternalStorageIntoRecyclerView()
                Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                // Sinon, informez l'utilisateur de l'échec
                Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show() // Gérez le cas où `bitmap` est null
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter { photo ->
            // Confirmez la suppression avec l'utilisateur avant de continuer
            AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Yes") { _, _ ->
                    // Si l'utilisateur confirme, supprimez la photo du stockage interne
                    val isDeletionSuccessful = deletePhotoFromInternalStorage(photo.name)
                    if (isDeletionSuccessful) {
                        // Si la suppression réussit, mettez à jour l'interface utilisateur
                        loadPhotosFromInternalStorageIntoRecyclerView()
                        Toast.makeText(this, "Photo successfully deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        // Si la suppression échoue, informez l'utilisateur
                        Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }

        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            bitmap?.let { nonNullBitmap ->
                val isSavedSuccessfully = savePhotoToInternalStorage(UUID.randomUUID().toString(), nonNullBitmap)
                if(isSavedSuccessfully) {
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show()
        }

        binding.btnTakePhoto.setOnClickListener {
            takePhoto.launch(null)
        }

        setupInternalStorageRecyclerView()
        loadPhotosFromInternalStorageIntoRecyclerView()
    }

    private fun setupInternalStorageRecyclerView() = binding.rvPrivatePhotos.apply {
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    private fun loadPhotosFromInternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    private fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadPhotosFromInternalStorage(): List<InternalStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val files = filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                InternalStoragePhoto(it.name, bmp)
            } ?: listOf()
        }
    }

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap): Boolean {
        return try {
            openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            true
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }
}