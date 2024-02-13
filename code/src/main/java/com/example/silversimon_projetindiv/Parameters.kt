package com.example.silversimon_projetindiv

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

        // Changer les prénoms des patients
        editTextPrenomPatient.setText(prenomPseudo)
        buttonEnregistrer.setOnClickListener {

            val nouveauPrenom = editTextPrenomPatient.text.toString()

            val editor = sharedPreferences.edit()
            editor.putString("prenom", nouveauPrenom)
            editor.apply()

            Toast.makeText(this, "Modifications enregistrées", Toast.LENGTH_SHORT).show()
        }

        // Retourner au début
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }



    }
}