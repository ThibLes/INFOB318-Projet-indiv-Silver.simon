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

/**
 * Activity that displays photos taken by the user.
 *
 * This activity allows the user to take photos and save them in the device's internal storage.
 * Saved photos are displayed in a RecyclerView.
 *
 * @author Thibaut Lesage
 */
class   Gallery : AppCompatActivity(){

    // Ask for camera permission (it's mandatory now, they can choose to not give it/Always give it / Just this time)
    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    // Binding for the activity and the adapter for the RecyclerView
    private lateinit var binding: ActivityGalleryBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter

    // ActivityResultLauncher for taking photos ( it's good for security and privacy)
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // We check if the bitmap is not null
        bitmap?.let { nonNullBitmap ->
            // If the bitmap is not null, we ask the user for the first name
            askForFirstName { firstName, gender ->
                // We generate a unique filename for the photo
                val filename = UUID.randomUUID().toString()
                // We (try to) save the photo in the internal storage of the device
                val isSavedSuccessfully = savePhotoToInternalStorage(filename, nonNullBitmap, firstName, gender?: "Non spécifié",5)
                // We check if the photo has been saved successfully
                assert(isSavedSuccessfully) { "Echec de l'enregistrement de la photo."}
                if(isSavedSuccessfully) {
                    // If the photo has been saved successfully, we update the RecyclerView
                    loadPhotosFromInternalStorageIntoRecyclerView()
                    // We inform the user that the photo has been saved successfully
                    Toast.makeText(this, "Photo enregistrée avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    // If the photo has not been saved successfully, we inform the user
                    Toast.makeText(this, "Echec", Toast.LENGTH_SHORT).show()
                }
            }
        } ?: // If the user leaves the camera without taking a photo, we inform him
        Toast.makeText(this, "Aucune photo n'a été prise", Toast.LENGTH_SHORT).show() //
    }

    /**
     * Ask the user for the first name of the person in the photo.
     *
     * @param afterNameProvided The function to call after the user has provided a first name.
     *
     * @author Thibaut Lesage
     * @version 1.0
     */
    private fun askForFirstName(afterNameProvided: (String, String) -> Unit) {
        val input = EditText(this)
        // We ask the user to enter the first name with a dialog box
        AlertDialog.Builder(this)
            .setTitle("Entrez le prénom")
            .setMessage("Entrez le prénom de la personne sur la photo.")
            .setView(input)
            .setPositiveButton("Next") { dialog, which ->
                // We get the first name entered by the user
                val firstName = input.text.toString()
                // If the first name is not empty, we ask
                if(firstName.isNotBlank()) {
                    askForGender { gender ->
                        // We call the function afterNameProvided with the first name and the gender
                        afterNameProvided(firstName, gender)
                    }
                }
            }
                // We propose to the user to cancel the action
            .setNegativeButton("Cancel", { dialog, which -> dialog.cancel() })
            .show()
    }

    /**
     * Ask the user for the gender of the person on the photo.
     *
     * @param afterGenderProvided The function to call after the user has provided the gender
     *
     * @version 1.1
     * @author Thibaut Lesage
     */
    private fun askForGender(afterGenderProvided: (String) -> Unit) {
        // We ask the user to select a gender between "Homme", "Femme" and "Non-binaire"
        val gender = arrayOf("Homme", "Femme", "Non-binaire")
        // We display the dialog box
        AlertDialog.Builder(this)
            .setTitle("Sélectionnez le genre de la personne sur la photo")
            .setItems(gender) { _, which ->
                // recall the function afterGenderProvided
                afterGenderProvided(gender[which])
            }
            .show()
    }




    /**
     * Called when the activity is created.
     * It initializes the activity, sets the content view, and sets up the RecyclerView.
     *
     * @author Thibaut Lesage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ask for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // If the permission is not granted, we ask the user for it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        // Animation for the slide up
        val optionsSlideUp = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_bottom,
            R.anim.slide_out_up
        )
        val buttonHome = findViewById<ImageView>(R.id.imageHome)
        // when the user clicks on the buttonHome, the user is redirected to the main activity
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent,optionsSlideUp.toBundle())
        }

        // animation for the slide left
        val optionsSlideLeft = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
        val buttonBack = findViewById<ImageView>(R.id.backImg)
        // when the user clicks on the buttonBack, the user is redirected to the parameters activity
        buttonBack.setOnClickListener {
            val intent = Intent(this, Parameters::class.java)
            startActivity(intent,optionsSlideLeft.toBundle())
        }

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter { photo ->
            // We ask the user if he wants to delete the photo
            AlertDialog.Builder(this)
                .setTitle("Supprimer la photo")
                .setMessage("Etes-vous sûr de vouloir supprimer cette photo ?")
                .setPositiveButton("Oui") { _, _ ->
                    // If the user wants to delete the photo, we try to delete it with the function deletePhotoFromInternalStorage
                    val isDeletionSuccessful = deletePhotoFromInternalStorage(photo.name)
                    if (isDeletionSuccessful) {
                        // If the deletion is successful, we update the RecyclerView
                        loadPhotosFromInternalStorageIntoRecyclerView()
                        Toast.makeText(this, "Photo supprimée avec succès", Toast.LENGTH_SHORT).show()
                    } else {
                        // If the deletion is not successful, we inform the user
                        Toast.makeText(this, "Échec de la suppression de la photo", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Non", null)
                .show()
        }

        // When the user clicks on the button to take a photo, the camera is launched
        binding.btnTakePhoto.setOnClickListener {
            takePhoto.launch(null)
        }
        // We configure the RecyclerView to display the photos from the internal storage of the device and load them
        setupInternalStorageRecyclerView()
        loadPhotosFromInternalStorageIntoRecyclerView()
    }

    /**
     * Setup the RecyclerView that displays the photos stored in the internal memory of the device.
     *
     * @author Thibaut Lesage
     * @version 1.1
     */
    private fun setupInternalStorageRecyclerView() = binding.rvPrivatePhotos.apply {
        // We set the adapter and the layout manager for the RecyclerView
        adapter = internalStoragePhotoAdapter
        // We use a StaggeredGridLayoutManager with 3 columns ( of photos ) and a vertical orientation
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    /**
     * Load the photos from the internal storage of the device into the RecyclerView.
     *
     * This function is called when the activity is created and when a photo is deleted.
     *
     * @author Thibaut Lesage
     * @version 1.2
     */
    private fun loadPhotosFromInternalStorageIntoRecyclerView() {
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            // We submit the list of photos to the adapter ( all the photos in the RecyclerView )
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    /**
     * Deletes a photo from the internal storage of the device.
     *
     * @param filename The filename of the photo to be deleted.
     * @return true if the photo has been deleted, false otherwise.
     *
     * @version 1.1
     * @author Thibaut Lesage
     */
    private fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    /**
     * Load the photos from the internal storage of the device.
     *
     * @return A list of [InternalStoragePhoto] objects representing the photos stored in the internal memory of the device.
     *
     * @version 1.3
     * @author Thibaut Lesage
     */
    private suspend fun loadPhotosFromInternalStorage(): List<InternalStoragePhoto> {
        // We use the IO dispatcher to read the files from the internal storage
        return withContext(Dispatchers.IO) {
            // We get the list of files in the internal storage
            val files = filesDir.listFiles()
            // We get the shared preferences for the name of the patient
            val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.mapNotNull {
                // We read the bytes of the file and convert them into a bitmap
                val bytes = it.readBytes()
                // We decode the bytes into a bitmap
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val filenameWithoutExtension = it.nameWithoutExtension
                // We get the name of the patient
                val namePatient = sharedPref.getString("$filenameWithoutExtension-name", null)
                // We get the gender of the patient
                val gender = sharedPref.getString("$filenameWithoutExtension-gender", "Non spécifié") ?: "Non spécifié"
                // We get the coefficient of the photo
                val coff = sharedPref.getInt("$filenameWithoutExtension-coff", 5)
                // We return the InternalStoragePhoto object if the name of the patient is not null ( that he exists )
                if (namePatient != null) {
                    InternalStoragePhoto(it.name, bmp, namePatient, gender, coff)
                } else {
                    null
                }
            } ?: listOf()
        }
    }

    /**
     * Saves a photo to the camera's internal storage.
     *
     * @param filename The unique file name for the photo.
     * @param bmp The bitmap of the photo to be saved.
     * @param namePatient The first name of the person in the photo.
     * @return true if the photo has been successfully saved, false otherwise.
     * @throws IOException If the photo cannot be saved.
     *
     * @version 1.5
     * @auhtor Thibaut Lesage
     *
     */
    private fun savePhotoToInternalStorage(filename: String, bmp: Bitmap, namePatient: String, gender : String, coff : Int): Boolean {
        // We check if the filename is not empty
        assert(filename.isNotBlank()) { "Le nom de fichier ne doit pas être vide" }
        // We add the extension .jpg to the filename
        val completeFilename = "$filename.jpg"
        return try {
            // We save the bitmap in the internal storage of the device
            openFileOutput(completeFilename, MODE_PRIVATE).use { stream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    // If the bitmap cannot be saved, we throw an IOException
                    throw IOException("Couldn't save bitmap.")
                }
            }
            // We get the shared preferences of the patient
            val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
            with(sharedPref.edit()) {
                // We put the name of the patient, the gender and the coefficient in the shared preferences
                putString("$filename-name", namePatient)
                putString("$filename-gender", gender)
                putInt("$filename-coff", coff)
                apply()
            }
            true
        } catch (e: IOException) {
            // If the photo cannot be saved, we print the stack trace
            e.printStackTrace()
            false
        }
    }
}