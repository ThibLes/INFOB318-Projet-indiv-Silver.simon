package com.example.silversimon_projetindiv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonCommencer = findViewById<Button>(R.id.buttonCommencer)
        val buttonParameters = findViewById<ImageView>(R.id.imageParameters)
        var clickCount = 0

        buttonCommencer.setOnClickListener {
            val intent = Intent(this, com.example.silversimon_projetindiv.Game::class.java)
            startActivity(intent)
        }
        buttonParameters.setOnClickListener {
            clickCount++
            if (clickCount == 3) {
                clickCount = 0
                val intent = Intent(this, com.example.silversimon_projetindiv.Parameters::class.java)
                startActivity(intent)
            }


        }
    }
}