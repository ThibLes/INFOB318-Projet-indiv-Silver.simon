package com.example.silversimon_projetindiv

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class Game : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Retourner au début
        val buttonHome = findViewById<ImageView>(R.id.imageHome)
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val textViewPrenomPatient = findViewById<TextView>(R.id.textViewPrenomPatient)
        // Accéder aux mêmes préférences partagées pour lire le prénom
        val sharedPreferences = getSharedPreferences("PrenomPatient", Context.MODE_PRIVATE)
        val prenomPatient = sharedPreferences.getString("prenom", "Inconnu")

        // Définir le texte de textViewPrenomPatient pour inclure le prénom du patient
        textViewPrenomPatient.text = getString(R.string.bonjour_patient, prenomPatient)

        // récuperer le bouton suivvant
        val buttonNext = findViewById<Button>(R.id.buttonNext)


        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            randomPhotoId?.let {
                setImageFromInternalStorage(it, imageViewPhotoPersonne)
            }
        }

        // permet de relancer les photos
        buttonNext.setOnClickListener {
            // Utilise lifecycleScope pour lancer une coroutine qui exécute la logique de chargement d'une nouvelle image
            lifecycleScope.launch {
                val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
                val randomPhotoId = getRandomPhotoIdFromInternalStorage()
                randomPhotoId?.let {
                    setImageFromInternalStorage(it, imageViewPhotoPersonne)
                }
            }
        }

    }
    private suspend fun getRandomPhotoIdFromInternalStorage(): String? {
        return withContext(Dispatchers.IO) {
            val files = applicationContext.filesDir.listFiles { _, name -> name.endsWith(".jpg") }
            files?.let {
                if (it.isNotEmpty()) {
                    val randomIndex = it.indices.random()
                    it[randomIndex].name
                } else {
                    null // Retourne null si aucun fichier n'est trouvé
                }
            }
        }
    }


    private fun setImageFromInternalStorage(filename: String, imageView: ImageView) {
        try {
            val fileInputStream = openFileInput(filename)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close() // toujours fermer le FileInputStream après utilisation pas oublier
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            // peut-être informer l'utilisateur que le chargement de l'image a échoué?????????
        }
    }



}