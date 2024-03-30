package com.example.silversimon_projetindiv

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    /**
     * Creates the main activity
     *
     * This activity is the first one to be launched when the application is started.
     * It contains two buttons: "Commencer" and "Paramètres".
     * The "Commencer" button launches the game activity, while the "Paramètres" button launches the parameters activity.
     *
     * Variables are created for user interface elements (Button and ImageView).
     * More explanations are given in the code.
     *
     * @author Thibaut Lesage
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Get the "Commencer" and "Paramètres" buttons from the layout file activity_main.xml
        val buttonCommencer = findViewById<Button>(R.id.buttonCommencer)
        val buttonParameters = findViewById<ImageView>(R.id.imageParameters)
        // Check if the buttons have been found and that the layout has loaded correctly
        assert(buttonCommencer != null) { "Le bouton 'Commencer' n'a pas été trouvé, problème de chargement." }
        assert(buttonParameters != null) { "Le bouton 'Paramètres' n'a pas été trouvé, problème de chargement." }
        // Animation for the slide right
        val optionsSlideRight = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        // when the user clicks on the "Commencer" button, the game activity is launched
        buttonCommencer.setOnClickListener {
            val button = Intent(this, com.example.silversimon_projetindiv.Game::class.java)
            startActivity(button, optionsSlideRight.toBundle())
        }
        // when the user clicks on the "Paramètres" button, the parameters activity is launched
        buttonParameters.setOnClickListener {
            val button = Intent(this, com.example.silversimon_projetindiv.Parameters::class.java)
            startActivity(button, optionsSlideRight.toBundle())
        }
    }

}