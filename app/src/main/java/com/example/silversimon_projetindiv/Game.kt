package com.example.silversimon_projetindiv

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
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
    private var incorrectTry = mutableMapOf<String,Boolean>()

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

                // setImageFromInternalStorage(it, imageViewPhotoPersonne)
            }
        }

    }
    private suspend fun getRandomPhotoIdFromInternalStorage(): String? {
        val difficultyLevel = getDifficultyLevel()
        return withContext(Dispatchers.IO) {
            val files = applicationContext.filesDir.listFiles { _, name -> name.endsWith(".jpg") }
            files?.let { fileList ->
                when (difficultyLevel) {
                    "facile" -> fileList.filter { file ->
                        val coff = getPhotoCoff(file.nameWithoutExtension)
                        coff <= 3
                    }
                    "moyen" -> fileList.filter { file ->
                        val coff = getPhotoCoff(file.nameWithoutExtension)
                        coff in 4..7
                    }
                    "difficile" -> fileList.filter { file ->
                        val coff = getPhotoCoff(file.nameWithoutExtension)
                        coff >= 8
                    }
                    "normal" -> fileList.toList()
                    else -> fileList.toList()
                }.randomOrNull()?.name
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
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            randomPhotoId?.let {
                setImageFromInternalStorage(it, imageViewPhotoPersonne)
            }
            setupGame()
        }
    }
    private suspend fun PropositionButtons(propositions: List<String>, correctName: String) {

        val buttons = listOf(
            findViewById<Button>(R.id.buttonFirstChoice),
            findViewById<Button>(R.id.buttonSecondChoice),
            findViewById<Button>(R.id.buttonThirdChoice),
            findViewById<Button>(R.id.buttonFourthChoice)
        )
        val randomPhotoId = getRandomPhotoIdFromInternalStorage()
        val shuffledPropositions = propositions.shuffled()
        if (randomPhotoId != null ) {
            buttons.forEachIndexed { index, button ->
                button.text = shuffledPropositions[index]
                button.setOnClickListener { clickedButton ->
                    val isCorrect = button.text == correctName
                    if (!isCorrect) {
                        incorrectTry[randomPhotoId] = true
                        Log.d("GameActivity", "dedans : ${randomPhotoId}")
                    }

                    // Appliquer la couleur en fonction de la réponse
                    clickedButton.setBackgroundColor(
                        ContextCompat.getColor(
                            this, if (isCorrect) R.color.green else R.color.red
                        )
                    )

                    if (isCorrect) {
                        val musique = MediaPlayer.create(applicationContext, R.raw.goodanswer)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.start()
                    }
                    Log.d("GameActivity", "ok ou non: ${isCorrect}")


                    if (isCorrect && incorrectTry[randomPhotoId] == true) {
                        incorrectTry.remove(randomPhotoId)
                        Log.d("GameActivity", "out : ${randomPhotoId}")
                    } else if (isCorrect) {
                        changePhotoCoff(randomPhotoId, true)
                    } else {
                        if (incorrectTry[randomPhotoId] != true) {
                            incorrectTry[randomPhotoId] = true
                            Log.d("GameActivity", "wytusfqytxf : ${randomPhotoId}")
                            changePhotoCoff(randomPhotoId, false)
                        }
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        clickedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
                        if (isCorrect) {
                            loadNextImage()
                        }
                    }, 1500)
                }
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

            }
        }
    }
    private fun getDifficultyLevel(): String {
        val difficulty = getSharedPreferences("GameDifficulty", Context.MODE_PRIVATE)
        return difficulty.getString("Difficulty", "normal") ?: "normal"
        Log.d("GameActivity", "difficulté: ${difficulty}")

    }
    private fun changePhotoCoff(filename: String, isCorrect: Boolean) {
        val photoname = getPhotoCoff(filename.substringBeforeLast("."))
        val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
        val currentCoff = sharedPref.getInt("$photoname-coff", 5)
        val newCoff = when {
            isCorrect && currentCoff > 1 -> currentCoff -1
            !isCorrect && currentCoff < 10 -> currentCoff + 1
            else -> currentCoff
        }
        Log.d("GameActivity", "nouveaucoff: ${newCoff}")

        with(sharedPref.edit()) {
            putInt("$photoname-coff", newCoff)
            apply()
        }
    }

    private fun getPhotoCoff(photoname: String): Int {
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("$photoname-coff", 5)
    }

}