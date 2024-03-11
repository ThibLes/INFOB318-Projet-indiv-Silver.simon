package com.example.silversimon_projetindiv

import android.content.Intent
import android.Manifest
import android.app.ActivityOptions
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var binding: ActivityGalleryBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter

    // Ajoutez une variable pour le résultat de la prise de photo
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // Vérifiez si nous avons reçu un bitmap non-null
        bitmap?.let { nonNullBitmap ->
            // Demandez le prénom après avoir pris la photo
            askForFirstName { firstName, gender ->
                // Générez un nom de fichier unique pour la photo
                val filename = UUID.randomUUID().toString()
                // Essayez d'enregistrer la photo avec le prénom dans le stockage interne
                val isSavedSuccessfully = savePhotoToInternalStorage(filename, nonNullBitmap, firstName, gender?: "Non spécifié")
                if(isSavedSuccessfully) {
                    // Si la sauvegarde a réussi, rechargez les photos et affichez un message
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    Toast.makeText(this, "Photo saved successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Sinon, informez l'utilisateur de l'échec
                    Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show() // Gérez le cas où `bitmap` est null
    }

    private fun askForFirstName(afterNameProvided: (String, String) -> Unit) {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Entrez le prénom")
            .setMessage("Entrez le prénom de la personne sur la photo.")
            .setView(input)
            .setPositiveButton("Next") { dialog, which ->
                val firstName = input.text.toString()
                if(firstName.isNotBlank()) {
                    askForGender { gender ->
                        afterNameProvided(firstName, gender)
                    }
                }
            }
            .setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })
            .show()
    }

    private fun askForGender(afterGenderProvided: (String) -> Unit) {
        val gender = arrayOf("Homme", "Femme", "Non-binaire", "Non spécifié")
        AlertDialog.Builder(this)
            .setTitle("Sélectionnez le genre de la personne sur la photo")
            .setItems(gender) { dialog, which ->
                afterGenderProvided(gender[which])
            }
            .show()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Vérifiez si la permission CAMERA est déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Si la permission n'est pas accordée, demandez-la à l'utilisateur
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        val optionsSlideUp = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_bottom,
            R.anim.slide_out_up
        )
        val buttonHome = findViewById<ImageView>(R.id.imageHome)
        // Retourner au début
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent,optionsSlideUp.toBundle())
        }


        val optionsSlideLeft = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        val buttonBack = findViewById<ImageView>(R.id.backImg)
        // Retourner aux paramètres
        buttonBack.setOnClickListener {
            val intent = Intent(this, Parameters::class.java)
            startActivity(intent,optionsSlideLeft.toBundle())
        }

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter { photo ->
            // confirmez la suppression avec l'utilisateur avant de continuer
            AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Yes") { _, _ ->
                    val isDeletionSuccessful = deletePhotoFromInternalStorage(photo.name)
                    if (isDeletionSuccessful) {
                        // mettere à jour l'interface utilisateur
                        loadPhotosFromInternalStorageIntoRecyclerView()
                        Toast.makeText(this, "Photo successfully deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        // au cas ou on echoue dde supprimer
                        Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
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
            val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.mapNotNull {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val filenameWithoutExtension = it.nameWithoutExtension
                val namePatient = sharedPref.getString("$filenameWithoutExtension-name", null)
                val gender = sharedPref.getString("$filenameWithoutExtension-gender", "Non spécifié") ?: "Non spécifié"
                if (namePatient != null) {
                    InternalStoragePhoto(it.name, bmp, namePatient, gender)
                } else {
                    null
                }
            } ?: listOf()
        }
    }

    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap, namePatient: String, gender : String): Boolean {
        // Le nom du fichier reste inchangé, seul pour la photo
        val completeFilename = "$filename.jpg"
        return try {
            openFileOutput(completeFilename, MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("Couldn't save bitmap.")
                }
            }
            // Enregistrement du nom du patient + genre dans les préférences partagées
            val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("$filename-name", namePatient)
                putString("$filename-gender", gender)
                Log.d("SavePhoto", "gender photo saved: $gender") /// !!!!


                apply()
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}