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

        val buttonCommencer = findViewById<Button>(R.id.buttonCommencer)
        val buttonParameters = findViewById<ImageView>(R.id.imageParameters)
        val optionsSlideRight = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )


        buttonCommencer.setOnClickListener {
            val intentGame = Intent(this, com.example.silversimon_projetindiv.Game::class.java)
            startActivity(intentGame, optionsSlideRight.toBundle())
        }

        buttonParameters.setOnClickListener {
            val intentGame = Intent(this, com.example.silversimon_projetindiv.Parameters::class.java)
            startActivity(intentGame, optionsSlideRight.toBundle())
        }
    }

}