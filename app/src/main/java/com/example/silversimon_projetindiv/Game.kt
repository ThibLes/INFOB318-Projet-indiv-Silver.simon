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

/**
 * Main game activity.
 *
 * This activity manages the flow of the game by displaying photos and asking questions
 * about the first names of the people photographed.
 *
 * The photos are stored in the application's internal storage, and the correct first names is stored in the photos' metadata.
 *
 * @author Thibaut Lesage
 */
class Game : AppCompatActivity() {

    // Inittialized the question object and a map to store the incorrect tries
    private lateinit var question: Question
    private var incorrectTry = mutableMapOf<String,Boolean>()

    /**
     * Creates the game activity.
     *
     * This activity is launched when the user clicks on the "Commencer" button in the main activity.
     * It displays the photo of a person and asks the user to find the correct first name among four proposals.
     *
     * The user can return to the main activity by clicking on the "Accueil" button.
     *
     * More explanations are given in the code.
     *
     * @author Thibaut Lesage
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Load the first image and start the game.
        loadNextImage()

        // Animation for the slide left
        val optionsSlideLeft = ActivityOptions.makeCustomAnimation(
            this,
            // R.anim.slide_in_left and R.anim.slide_out_right are the animations used to slide the activity to the left
            // stored in the res/anim directory
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )


        // When the user clicks on the "Accueil" button, the main activity is launched
        val buttonHome = findViewById<ImageView>(R.id.imageHome)
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent,optionsSlideLeft.toBundle())
        }

        val textViewFNPatient = findViewById<TextView>(R.id.textViewPrenomPatient)
        // Get the patient's first name from the shared preferences
        val sharedPreferences = getSharedPreferences("PrenomPatient", Context.MODE_PRIVATE)
        val prenomPatient = sharedPreferences.getString("prenom", "Inconnu")

        // Display the patient's first name in the text view
        textViewFNPatient.text = getString(R.string.bonjour_patient, prenomPatient)
        // Initialize the question object
        question = Question(this)
    }

    /**
     * retrieves a random photo ID from the application's internal storage
     * based on the current difficulty level stored in shared preferences.
     *
     *
     * @return A random photo ID, or `null` if there are no photos.
     * @author Thibaut Lesage
     * @version 1.3
     *
     * Suspend function pauses coroutine execution until the result is ready.
     * This avoids blocking the main thread.
     */
    private suspend fun getRandomPhotoIdFromInternalStorage(): String? {
        // Get the current difficulty level by using the getDifficultyLevel function
        val difficultyLevel = getDifficultyLevel()
        // Check if the difficulty level is valid
        assert(
            difficultyLevel in listOf("facile", "moyen", "difficile", "normal"),
            { "Niveau de difficulté inconnu" }
        )
        // Get the list of files in the internal storage directory
        return withContext(Dispatchers.IO) {
            val files = applicationContext.filesDir.listFiles { _, name -> name.endsWith(".jpg") }
            // Filter the list of files based on the difficulty level
            files?.let { fileList ->
                when (difficultyLevel) {
                    // If the difficulty level is "facile", the coefficient of the photo must be less than or equal to 3
                    "facile" -> fileList.filter { file ->
                        val coff = getPhotoCoff(file.nameWithoutExtension)
                        coff <= 3
                    }
                    // If the difficulty level is "moyen", the coefficient of the photo must be between 4 and 7
                    "moyen" -> fileList.filter { file ->
                        val coff = getPhotoCoff(file.nameWithoutExtension)
                        coff in 4..7
                    }
                    // If the difficulty level is "difficile", the coefficient of the photo must be greater than or equal to 8
                    "difficile" -> fileList.filter { file ->
                        val coff = getPhotoCoff(file.nameWithoutExtension)
                        coff >= 8
                    }
                    // If the difficulty level is "normal", all photos are available
                    "normal" -> fileList.toList()
                    else -> fileList.toList()
                }.randomOrNull()?.name
            }
        }
    }


    /**
     * Loads an image from the application's internal storage and displays it in an `ImageView`.
     *
     * @param filename The filename of the image to be loaded.
     * @param imageView The `ImageView` in which to display the image.
     * @throws IOException If the file cannot be opened or read.
     * @version 1.1
     * @author Thibaut Lesage
     */
    private fun setImageFromInternalStorage(filename: String, imageView: ImageView) {
        try {
            val fileInputStream = openFileInput(filename)
            // Decode the file input stream into a bitmap
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close() // Close the file input stream, always close the file after reading it
            // Display the image in the ImageView
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            // If the file cannot be opened or read, print the stack trace
            e.printStackTrace()
        }
    }

    /**
     * Return the correct name of a photo from its identifier.
     *
     * @param photoId The photo identifier.
     * @return The photo's correct name.
     *
     * @version 1.0
     * @author Thibaut Lesage
     */
    private fun getCorrectName(photoId: String): String {
        val filename = photoId.substringBeforeLast(".") // because otherwise the .jpg is included in the name and it is not found in the shared preferences
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        // Get the correct name of the photo from the shared preferences (if it doesn't exist, return "Inconnu")
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


    /**
     * Load the next image.
     *
     * Detailed information is given in the code.
     *
     * @see getRandomPhotoIdFromInternalStorage
     * @see setupGame
     *
     * @version 1.4
     * @author Thibaut Lesage
     */
    private fun loadNextImage() {
        // LyfecycleScope is used to launch a coroutine in the lifecycle of the activity
        // It's useful to avoid memory leaks and to cancel the coroutine when the activity is destroyed
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            // Get a random photo ID from the internal storage using the getRandomPhotoIdFromInternalStorage function
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            // If there is at least one photo that correspond to the level difficulty in the internal storage (we check this in the getRandomPhotoIdFromInternalStorage function)
            // then we set up the game or the next image
            if (randomPhotoId != null) {
            randomPhotoId?.let {
                setupGame()
            }} else // If there is no photo in the internal storage (with the right coff ), we display a default image
            { runOnUiThread { imageViewPhotoPersonne.setImageResource(R.drawable.nophoto) }}
        }
    }

    /**
     * Generates proposal buttons for a question.
     *
     * We take the correct name and 3 other random names of the same gender for the propositions and display them on the buttons.
     *
     * @param proposals The list of name proposals.
     * @param correctName The correct name to find.
     * @param randomPhotoId The id of the photo.
     *
     * @version 1.6
     * @author Thibaut Lesage
     */
    private fun propositionButtons(proposals: List<String>, correctName: String, randomPhotoId : String) {

        // Get the all the buttons from the layout file activity_game.xml and store them in a list
        val buttons = listOf(
            findViewById<Button>(R.id.buttonFirstChoice),
            findViewById<Button>(R.id.buttonSecondChoice),
            findViewById<Button>(R.id.buttonThirdChoice),
            findViewById<Button>(R.id.buttonFourthChoice)
        )
        val shuffledPropositions = proposals.shuffled()
        // For each button, display a proposition and set a click listener
            buttons.forEachIndexed { index, button ->
                // Each button displays a proposition
                button.text = shuffledPropositions[index]
                button.setOnClickListener { clickedButton ->
                    // If the user clicks on a button, we check if the answer is correct
                    val isCorrect = button.text == correctName
                    // If the answer is not correct
                    if (!isCorrect) {
                        // We store the incorrect try in a map
                        incorrectTry[randomPhotoId] = true
                        // We play a sound when the answer is wrong
                        val musique = MediaPlayer.create(applicationContext, R.raw.wrongsong)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.setVolume(1f,1f)
                        musique.start()
                    }

                    // Change the background color of the button to green if the answer is correct, red otherwise
                    clickedButton.setBackgroundColor(
                        ContextCompat.getColor(
                            this, if (isCorrect) R.color.green else R.color.red
                        )
                    )
                    // Play a sound when the answer is correct
                    val musique = MediaPlayer.create(applicationContext, R.raw.goodanswer)
                    // If the answer is correct then the song

                    // If the answer is correct and the user has already made an incorrect try
                    if (isCorrect && incorrectTry[randomPhotoId] == true) {
                        // We remove the incorrect try from the map
                        incorrectTry.remove(randomPhotoId)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.start()
                    } else if (isCorrect) {
                        // If the answer is correct and not in the map, we change the coefficient of the photo
                        changePhotoCoff(randomPhotoId, true)
                        musique.setOnCompletionListener { mp -> mp.release() }
                        musique.start()
                    } else {
                        // If the answer is incorrect, we change the coefficient of the photo and store the incorrect try in the map
                        if (incorrectTry[randomPhotoId] != true) {
                            incorrectTry[randomPhotoId] = true
                            changePhotoCoff(randomPhotoId, false)
                        }
                    }

                    // Delay the next image loading by 1.5 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Reset the background color of the button to grey
                        clickedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.grey))
                        if (isCorrect) {
                            loadNextImage()
                        }
                    }, 1500)

                }
            }

    }

    /**
     * Set up the game by generating a question and displaying the photo and the propositions.
     */
    private fun setupGame() {
        lifecycleScope.launch {
            val imageViewPhotoPersonne = findViewById<ImageView>(R.id.imageViewPhotoPersonne)
            // Get a random photo ID from the internal storage using the getRandomPhotoIdFromInternalStorage function
            val randomPhotoId = getRandomPhotoIdFromInternalStorage()
            // If there is at least one photo that correspond to the level difficulty in the internal storage (we check this in the getRandomPhotoIdFromInternalStorage function)
            // We get the correct name and genre of the photo and generate the question
            if (randomPhotoId != null) {
                randomPhotoId?.let {
                    val correctName = getCorrectName(it)
                    val correctGenre = getCorrectGenre(it)
                    val propositions = question.generateQuestion(correctGenre, correctName)

                    setImageFromInternalStorage(it, imageViewPhotoPersonne)
                    propositionButtons(propositions, correctName, randomPhotoId)
                }
            } else {
                // If there is no photo in the internal storage (with the right coff ), we display a default image
                // In case the user has already answered all the photos
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
     * Modify the coefficient of a photo.
     *
     * @param filename The name of the photo.
     * @param isCorrect If the answer is correct.
     *
     * @version 1.1
     * @author Thibaut Lesage
     */
    private fun changePhotoCoff(filename: String, isCorrect: Boolean) {
        // Get the name of the photo without the extension .jpg
        val photoname = getPhotoCoff(filename.substringBeforeLast("."))
        val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
        // Get the current coefficient of the photo, the default value is 5
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
     *
     * @version 1.0
     * @author Thibaut Lesage
     */
    private fun getPhotoCoff(photoname: String): Int {
        val sharedPreferences = getSharedPreferences("PhotoMetadata", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("$photoname-coff", 5)
    }

}