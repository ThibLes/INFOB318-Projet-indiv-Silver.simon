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

        val buttonModifierCode = findViewById<Button>(R.id.buttonModifierCode)
        val editTextNewCode = findViewById<EditText>(R.id.editTextNewCode)

        val buttonGallery = findViewById<Button>(R.id.buttonGalerie)
        //test



        // Changer les prénoms des patients
        editTextPrenomPatient.setText(prenomPseudo)
        buttonEnregistrer.setOnClickListener {

            val nouveauPrenom = editTextPrenomPatient.text.toString()

            val editor = sharedPreferences.edit()
            editor.putString("prenom", nouveauPrenom)
            editor.apply()

            Toast.makeText(this, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
        }



        // code mis sur emulateur 1234
        buttonModifierCode.setOnClickListener {
            val newCode = editTextNewCode.text.toString()
            if(newCode.isNotEmpty()) {
                // Enregistrer le nouveau code dans SharedPreferences
                val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("AccessCode", newCode)
                editor.apply()

                // Informer l'utilisateur que le code a été changé
                Toast.makeText(this, "Le nouveau code a été enregistré.", Toast.LENGTH_SHORT).show()
            } else {
                // Demander à l'utilisateur d'entrer un code s'il est vide
                Toast.makeText(this, "Veuillez entrer un nouveau code", Toast.LENGTH_SHORT).show()
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