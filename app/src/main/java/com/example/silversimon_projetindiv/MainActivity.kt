package com.example.silversimon_projetindiv

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Récupération des boutons et assignation de variables grâce à leurs ID
        val buttonCommencer = findViewById<Button>(R.id.buttonCommencer)
        val buttonParameters = findViewById<ImageView>(R.id.imageParameters)
        assert(buttonCommencer != null) { "Le bouton 'Commencer' n'a pas été trouvé." }
        assert(buttonParameters != null) { "Le bouton 'Paramètres' n'a pas été trouvé." }

        // Animation de changement d'écran vers la droite
        val optionsSlideRight = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )


        // Bouton qui nous permet de rejoindre la page "Game"
        buttonCommencer.setOnClickListener {
            val intentGame = Intent(this, com.example.silversimon_projetindiv.Game::class.java)
            startActivity(intentGame, optionsSlideRight.toBundle())
        }

        // Bouton qui nous permet de rejoindre la page "Paramètres"
        buttonParameters.setOnClickListener {
            val intentGame = Intent(this, com.example.silversimon_projetindiv.Parameters::class.java)
            startActivity(intentGame, optionsSlideRight.toBundle())
        }
    }

}