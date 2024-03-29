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

        // Charger la première image
        loadNextImage()

        // Animation de changement d'écran vers la gauche
        val optionsSlideLeft = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )


        // Retourner au début de l'application
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


        // VERIFIER SI CA SERT VRAIMENT 0 QQCHOSE
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            randomPhotoId?.let {
                val correctName = getCorrectName(it)
                val correctGenre = getCorrectGenre(it)
            }
        }

    }

    /**
     * Récupère un identifiant de photo aléatoire depuis le stockage interne de l'application
     * en fonction du niveau de difficulté actuel qui est stocké dans les préférences partagées.
     *
     * @return Un identifiant de photo aléatoire, ou `null` s'il n'y a pas de photos.
     */
    private suspend fun getRandomPhotoIdFromInternalStorage(): String? {
        val difficultyLevel = getDifficultyLevel()
        assert(
            difficultyLevel in listOf("facile", "moyen", "difficile", "normal"),
            { "Niveau de difficulté inconnu" }
        )
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


    /**
     * Charge une image depuis le stockage interne de l'application et l'affiche dans un `ImageView`.
     *
     * @param filename Le nom du fichier de l'image à charger.
     * @param imageView L'`ImageView` dans lequel afficher l'image.
     */
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

    /**
     * Récupère le nom correct d'une photo à partir de son identifiant.
     *
     * @param photoId L'identifiant de la photo.
     * @return Le nom correct de la photo.
     */
    private fun getCorrectName(photoId: String): String {
        val filename = photoId.substringBeforeLast(".") // car sinon le .jpg à la fin fait bug pour trouver les infos
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getString("$filename-name", "Inconnu") ?: "Inconnu"
    }

    /**
     * Récupère le genre d'une photo à partir de son identifiant.
     *
     * @param photoId L'identifiant de la photo.
     * @return Le genre correct de la photo.
     */
    private fun getCorrectGenre(photoId: String): String {
        val filename = photoId.substringBeforeLast(".")
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getString("$filename-gender", "nonGenre") ?: "nonGenre"
    }

    // permet de relancer les photos et setup le jeu
    /**
     * Charge la photo suivante et configure le jeu.
     */
    private fun loadNextImage() {
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            if (randomPhotoId == null) {
            randomPhotoId?.let {
                setImageFromInternalStorage(it, imageViewPhotoPersonne)
            }} else { runOnUiThread { imageViewPhotoPersonne.setImageResource(R.drawable.nophoto) }}
            setupGame()
        }
    }

    /**
     * Génère les boutons de proposition pour une question.
     *
     * Nous prenons le nom correct et 3 autres noms aléatoires du même genre pour les propositions.
     *
     * @param propositions La liste des propositions de noms.
     * @param correctName Le nom correct à trouver.
     */
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
                        val musique = MediaPlayer.create(applicationContext, R.raw.wrongsong)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.setVolume(1f,1f)
                        musique.start()
                        Log.d("GameActivity", "dedans : ${randomPhotoId}")
                    }

                    // Appliquer la couleur en fonction de la réponse
                    clickedButton.setBackgroundColor(
                        ContextCompat.getColor(
                            this, if (isCorrect) R.color.green else R.color.red
                        )
                    )
                    val musique = MediaPlayer.create(applicationContext, R.raw.goodanswer)
                    if (isCorrect && incorrectTry[randomPhotoId] == true) {
                        incorrectTry.remove(randomPhotoId)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.start()
                        Log.d("GameActivity", "out : ${randomPhotoId}")
                    } else if (isCorrect) {
                        changePhotoCoff(randomPhotoId, true)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.start()
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

    /**
     * Configure le jeu en affichant une photo et en générant une question.
     */
    private fun setupGame() {
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            if (randomPhotoId != null) {
                randomPhotoId?.let {
                    val correctName = getCorrectName(it)
                    val correctGenre = getCorrectGenre(it)
                    val propositions = question.generateQuestion(correctGenre, correctName)

                    setImageFromInternalStorage(it, imageViewPhotoPersonne)
                    PropositionButtons(propositions, correctName)
                }
            } else {
                imageViewPhotoPersonne.setImageResource(R.drawable.nophoto)
            }
            }
        }

    /**
     * Récupère le niveau de difficulté actuel du jeu qui est stocké dans les préférences partagées.
     *
     * @return Le niveau de difficulté actuel.
     */
    private fun getDifficultyLevel(): String {
        val difficulty = getSharedPreferences("GameDifficulty", Context.MODE_PRIVATE)
        return difficulty.getString("Difficulty", "normal") ?: "normal"
    }

    /**
     * Modifie le coefficient d'une photo.
     *
     * @param filename Le nom du fichier de la photo.
     * @param isCorrect `true` si la réponse était correcte, `false` sinon.
     */
    private fun changePhotoCoff(filename: String, isCorrect: Boolean) {
        val photoname = getPhotoCoff(filename.substringBeforeLast("."))
        val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
        val currentCoff = sharedPref.getInt("$photoname-coff", 5)
        val newCoff = when {
            isCorrect && currentCoff > 1 -> currentCoff -1
            !isCorrect && currentCoff < 10 -> currentCoff + 1
            else -> currentCoff
        }
        assert(currentCoff in 1..10) { "Le coefficient n'est pas dans la bonne tranche" }


        with(sharedPref.edit()) {
            putInt("$photoname-coff", newCoff)
            apply()
        }
    }

    /**
     * Récupère le coefficient d'une photo.
     *
     * @param photoname Le nom de la photo.
     * @return Le coefficient de la photo.
     */
    private fun getPhotoCoff(photoname: String): Int {
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("$photoname-coff", 5)
    }

}