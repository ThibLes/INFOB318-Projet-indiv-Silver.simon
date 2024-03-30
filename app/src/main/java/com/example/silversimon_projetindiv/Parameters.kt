package com.example.silversimon_projetindiv

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast

class Parameters : AppCompatActivity() {

    /**
     * Creates parameter activity
     *
     * This activity lets you change the patient's first name and the game's difficulty level.
     * The patient's first name and the difficulty level are stored in different shared preferences.
     *
     * Variables are created for user interface elements (EditText, Button, ImageView and Switch).
     * More explanations are given in the code.
     *
     * @author Thibaut Lesage
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content of the activity to the layout file activity_parameters.xml (in the res/layout directory)
        setContentView(R.layout.activity_parameters)
        // Get the edittextprenom, buttonenregistrer, imagehome, buttongalerie, rstniveau
        // and the switches from the layout file activity_parameters.xml
        val editTextPrenomPatient = findViewById<EditText>(R.id.editTextPrenom)
        val buttonEnregistrer = findViewById<Button>(R.id.buttonEnregistrer)
        // Get the shared preferences for the patient's first name
        val sharedPreferences = getSharedPreferences("PrenomPatient", Context.MODE_PRIVATE)
        val prenomPseudo = sharedPreferences.getString("prenom", "")
        val buttonHome = findViewById<ImageView>(R.id.imageHome)
        val buttonGallery = findViewById<Button>(R.id.buttonGalerie)
        val buttonReset = findViewById<Button>(R.id.rstNiveau)
        val switchEasy: Switch = findViewById(R.id.switchEasy)
        val switchMedium: Switch = findViewById(R.id.switchMedium)
        val switchHard: Switch = findViewById(R.id.switchHard)
        val AllSwitch = listOf(switchEasy, switchMedium, switchHard) // list of switches
        // Restore the state of the switches according to the difficulty level of the game (using the restoreSwitchState function)
        restoreSwitchState(AllSwitch)
        // Display the patient's first name in the EditText
        editTextPrenomPatient.setText(prenomPseudo)
        // When the user clicks on the "Enregistrer" button, the patient's first name is saved in the shared preferences
        buttonEnregistrer.setOnClickListener {
            val nouveauPrenom = editTextPrenomPatient.text.toString()
            val editor = sharedPreferences.edit()
            editor.putString("prenom", nouveauPrenom)
            editor.apply()
            // Display a message to the user to confirm that the modifications have been saved
            Toast.makeText(this, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
        }

        // When the user clicks on a switch, the difficulty level of the game is saved/changed in the shared preferences
        AllSwitch.forEach { switch ->
            switch.setOnCheckedChangeListener { idbutton, isChecked ->
                if (isChecked) {
                    // Only one switch can be checked at a time
                    AllSwitch.forEach { otherSwitch ->
                        if (otherSwitch != switch) otherSwitch.isChecked = false
                    }
                    // Get the difficulty level of the game according to the switch clicked
                    val difficultyGame = when (idbutton.id) {
                        R.id.switchEasy -> "easy"
                        R.id.switchMedium -> "moyen"
                        R.id.switchHard -> "difficile"
                        else -> "normal"
                    }
                    // Save the difficulty level of the game in the shared preferences (using the saveDifficulty function)
                    saveDifficulty(difficultyGame)
                } else {
                    // If no switch is checked, the difficulty level is set to "normal" and saved in the shared preferences (using the saveDifficulty function)
                    if (AllSwitch.none { it.isChecked }) {
                        saveDifficulty("normal")
                    }
                }
            }
        }
            // When the user clicks on the "Home" button, the user is redirected to the main activity
            buttonHome.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            // When the user clicks on the "Reset" button, the coefficients of the photos are reset to 5 (using the resetCoff function)
            buttonReset.setOnClickListener{
                resetCoff()
            }
            // Animation for the slide right
            val optionsSlideRight = ActivityOptions.makeCustomAnimation(
                this,
                // It uses the slide_in_right.xml and slide_out_left.xml files in the res/anim directory
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            // When the user clicks on the "Gallery" button, the user is redirected to the gallery activity
            buttonGallery.setOnClickListener {
                val intent = Intent(this, Gallery::class.java)
                startActivity(intent, optionsSlideRight.toBundle())
            }

        }

        /**
         * Saves the difficulty level of the game.
         *
         * This function saves the difficulty level of the game in the shared preferences.
         *
         * @param level The difficulty level of the game.
         *
         * @author Thibaut Lesage
         * @version 1.1
         */
        private fun saveDifficulty(level: String) {
                // Get the shared preferences for the difficulty level of the game (create one if it doesn't exist)
                // Private mode: only this application can read and write the shared preferences
                val sharedPref = getSharedPreferences("GameDifficulty", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("Difficulty", level)
                    apply()
                }

            }

        /**
         * Gets the difficulty level of the game.
         *
         * This function retrieves the difficulty level of the game from the shared preferences.
         *
         * @return The difficulty level of the game.
         *
         * @author Thibaut Lesage
         * @version 1.0
         */
        private fun getDifficultyLevel(): String {
            // Get the shared preferences for the difficulty level of the game
            // Private mode: only this application can read and write the shared preferences
            val sharedPreferences = getSharedPreferences("GameDifficulty", Context.MODE_PRIVATE)
            // Return the difficulty level of the game (if it doesn't exist, return "normal")
            return sharedPreferences.getString("Difficulty", "normal") ?: "normal"
        }

        /**
         * Restores the state of the switches.
         *
         * This function restores the state of the switches according to the difficulty level of the game.
         *
         * @param AllSwitch The list of switches.
         *
         * @author Thibaut Lesage
         * @version 1.1
         */
        private fun restoreSwitchState(AllSwitch: List<Switch>) {
            // get the current difficulty level by calling the getDifficultyLevel function
            val currentDifficulty = getDifficultyLevel()
            // only one switch can be checked
            AllSwitch.forEach { it.isChecked = false }
            // check the switch corresponding to the current difficulty and set it to true
            when (currentDifficulty) {
                "easy" -> AllSwitch[0].isChecked = true
                "moyen" -> AllSwitch[1].isChecked = true
                "difficile" -> AllSwitch[2].isChecked = true
            }
        }

         /**
          * Resets the coefficients of the photos.
          *
          * This function resets the coefficients of the photos to 5 (the default value).
          *
          * @author Thibaut Lesage
          * @version 1.0
          *
          */
        private fun resetCoff () {
             // Get the list of all the photos in the internal storage
            val photo = applicationContext.filesDir.listFiles { _, name -> name.endsWith(".jpg") }
             // Get the shared preferences for the coefficients of the photos
            val sharedPref = getSharedPreferences("PhotoMetadata", MODE_PRIVATE)
             // For each photo, we get the name of the photo (we remove the .jpg from the name) and reset the coefficient to 5
            photo?.forEach { file ->
                val photoName = file.name.substringBeforeLast(".")
                with(sharedPref.edit()) {
                    putInt("$photoName-coff", 5)
                    apply()
                }
            }
            // Display a message to the user to confirm that the coefficients have been reset
            Toast.makeText(this, "les coefficients ont bien été reset à 5", Toast.LENGTH_SHORT).show()
        }


}