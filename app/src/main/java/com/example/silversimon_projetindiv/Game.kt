package com.example.silversimon_projetindiv

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

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







}
}