package com.example.silversimon_projetindiv

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast

class Parameters : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters)

        val editTextPrenomPatient = findViewById<EditText>(R.id.editTextPrenom)
        val buttonEnregistrer = findViewById<Button>(R.id.buttonEnregistrer)

        val sharedPreferences = getSharedPreferences("PrenomPatient", Context.MODE_PRIVATE)
        val prenomPseudo = sharedPreferences.getString("prenom", "")

        val buttonHome = findViewById<ImageView>(R.id.imageHome)


        val buttonGallery = findViewById<Button>(R.id.buttonGalerie)
        //test

        val switchEasy: Switch = findViewById(R.id.switchEasy)
        val switchMedium: Switch = findViewById(R.id.switchMedium)
        val switchHard: Switch = findViewById(R.id.switchHard)
        val AllSwitch = listOf(switchEasy, switchMedium, switchHard)

        restoreSwitchState(AllSwitch)

        // Changer les prénoms des patients
        editTextPrenomPatient.setText(prenomPseudo)
        buttonEnregistrer.setOnClickListener {

            val nouveauPrenom = editTextPrenomPatient.text.toString()

            val editor = sharedPreferences.edit()
            editor.putString("prenom", nouveauPrenom)
            editor.apply()

            Toast.makeText(this, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
        }



        AllSwitch.forEach { switch ->
            switch.setOnCheckedChangeListener { idbutton, isChecked ->
                if (isChecked) {
                    AllSwitch.forEach { otherSwitch ->
                        if (otherSwitch != switch) otherSwitch.isChecked = false
                    }
                    val difficultyGame = when (idbutton.id) {
                        R.id.switchEasy -> "easy"
                        R.id.switchMedium -> "moyen"
                        R.id.switchHard -> "difficile"
                        else -> "normal"
                    }
                    saveDifficulty(difficultyGame)
                } else {
                    if (AllSwitch.none { it.isChecked }) {
                        saveDifficulty("normal")
                    }
                }
            }
        }

            // Retourner au début
            buttonHome.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            val optionsSlideRight = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            // Aller Galerie
            buttonGallery.setOnClickListener {
                val intent = Intent(this, Gallery::class.java)
                startActivity(intent, optionsSlideRight.toBundle())
            }

        }

         private fun saveDifficulty(level: String) {
            val sharedPref = getSharedPreferences("GameDifficulty", MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("Difficulty", level)
                apply()
            }

        }

        private fun getDifficultyLevel(): String {
            val sharedPreferences = getSharedPreferences("GameDifficulty", Context.MODE_PRIVATE)
            return sharedPreferences.getString("Difficulty", "normal") ?: "normal"
        }

        private fun restoreSwitchState(AllSwitch: List<Switch>) {
            val currentDifficulty = getDifficultyLevel()

            // Désactive tous les switches avant de restaurer l'état
            AllSwitch.forEach { it.isChecked = false }

            when (currentDifficulty) {
                "easy" -> AllSwitch[0].isChecked = true
                "moyen" -> AllSwitch[1].isChecked = true
                "difficile" -> AllSwitch[2].isChecked = true
            }
        }


}