package com.example.silversimon_projetindiv

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class Game : AppCompatActivity() {

    private lateinit var question: Question

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        loadNextImage()

        val optionsSlideLeft = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )



        // Retourner au début
        val buttonHome = findViewById<ImageView>(R.id.imageHome)
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent,optionsSlideLeft.toBundle())
        }

        val textViewPrenomPatient = findViewById<TextView>(R.id.textViewPrenomPatient)
        // Accéder aux mêmes préférences partagées pour lire le prénom
        val sharedPreferences = getSharedPreferences("PrenomPatient", Context.MODE_PRIVATE)
        val prenomPatient = sharedPreferences.getString("prenom", "Inconnu")

        // Définir le texte de textViewPrenomPatient pour inclure le prénom du patient
        textViewPrenomPatient.text = getString(R.string.bonjour_patient, prenomPatient)

        question = Question(this)

        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            randomPhotoId?.let {
                val correctName = getCorrectName(it)
                val correctGenre = getCorrectGenre(it)
                val propositions = question.generateQuestion(correctGenre,correctName)

                setImageFromInternalStorage(it, imageViewPhotoPersonne)
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

    private fun getCorrectName(photoId: String): String {
        val filename = photoId.substringBeforeLast(".") // car sinon le .jpg à la fin fait bug pour trouver les infos
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getString("$filename-name", "Inconnu") ?: "Inconnu"
    }

    private fun getCorrectGenre(photoId: String): String {
        val filename = photoId.substringBeforeLast(".")
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getString("$filename-gender", "nonGenre") ?: "nonGenre"
    }

    // permet de relancer les photos et setup le jeu
    private fun loadNextImage() {
        // Utilise lifecycleScope pour lancer une coroutine qui exécute la logique de chargement d'une nouvelle image
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            randomPhotoId?.let {
                setImageFromInternalStorage(it, imageViewPhotoPersonne)
            }
            setupGame()
        }
    }
    private fun PropositionButtons(propositions: List<String>,correctName: String) {

        val buttons = listOf(
            findViewById<Button>(R.id.buttonFirstChoice),
            findViewById<Button>(R.id.buttonSecondChoice),
            findViewById<Button>(R.id.buttonThirdChoice),
            findViewById<Button>(R.id.buttonFourthChoice)
        )

        // Mélange des propositions avec la réponse correcte à chaque appel
        val shuffledPropositions = propositions.shuffled()

        // Affectation des propositions mélangées aux boutons et ajout d'écouteurs d'événements
        buttons.forEachIndexed { index, button ->
            button.text = shuffledPropositions[index]
            button.setOnClickListener { clickedButton ->
                val isCorrect = button.text == correctName
                // Appliquer la couleur en fonction de la réponse
                clickedButton.setBackgroundColor(
                    ContextCompat.getColor(
                        this, if (isCorrect) R.color.green else R.color.red
                    )
                )

                // Créer un Handler pour rétablir la couleur initiale après 2 secondes
                Handler(Looper.getMainLooper()).postDelayed({
                    // Réinitialiser la couleur du bouton. Remplace `buttonOriginalColor` par la couleur d'origine de tes boutons.
                    clickedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
                    // Si la réponse est correcte, tu peux ici passer à la question suivante ou effectuer toute autre action souhaitée.
                    if (isCorrect) {
                        loadNextImage()
                    }
                }, 1500) // 2000 millisecondes = 2 secondes
            }
        }
    }

    private fun setupGame() {
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            randomPhotoId?.let {
                val correctName = getCorrectName(it)
                val correctGenre = getCorrectGenre(it)
                val propositions = question.generateQuestion(correctGenre, correctName)

                setImageFromInternalStorage(it, imageViewPhotoPersonne)
                PropositionButtons(propositions,correctName)
                Log.d("GameActivity", "Nom récupéré: ${randomPhotoId}")
                Log.d("GameActivity", "Nom récupéré: ${getCorrectName(randomPhotoId)}")
                Log.d("GameActivity", "Genre récupéré: ${getCorrectGenre(randomPhotoId)}")
            }
        }
    }

}