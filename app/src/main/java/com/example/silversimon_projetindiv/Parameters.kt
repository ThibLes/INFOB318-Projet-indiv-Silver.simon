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
            switch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    AllSwitch.forEach { otherSwitch ->
                        if (otherSwitch != switch) otherSwitch.isChecked = false
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
    }
}