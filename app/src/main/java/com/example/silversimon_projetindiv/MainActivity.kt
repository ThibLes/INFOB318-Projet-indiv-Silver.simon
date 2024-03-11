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
        var clickCount = 0
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
            clickCount++
            if (clickCount == 3) {
                clickCount = 0
                showCodeDialog()
            }
        }
    }


    private fun showCodeDialog() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val codeSecret = sharedPreferences.getString("AccessCode","") // Utilisez "AccessCode" comme clé

        // on vérifie d'abord si il y a déja un code enregistré
        if (codeSecret.isNullOrEmpty()) {
            val intentParameters = Intent(this, com.example.silversimon_projetindiv.Parameters::class.java)
            val optionsSlideDown = ActivityOptions.makeCustomAnimation(
                this,
                R.anim.slide_in_up,
                R.anim.slide_out_bottom
            )
            startActivity(intentParameters, optionsSlideDown.toBundle())
        }
        else
        {
            val demanderCode = EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }

            AlertDialog.Builder(this)
                .setTitle("Code d'accès pour paramètre")
                .setView(demanderCode)
                .setPositiveButton("Valider") { dialog, which ->
                    val code = demanderCode.text.toString()
                    if (validateCode(code)) {
                        startActivity(Intent(this, com.example.silversimon_projetindiv.Parameters::class.java))
                    } else {
                        Toast.makeText(this@MainActivity, "Code incorrect", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annuler", null)
                .show()
    }
}


    private fun validateCode(code: String): Boolean {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val storedCode = sharedPreferences.getString("AccessCode", "") // Utilisez "AccessCode" comme clé
        // Si aucun code n'est stocké ou si le code saisi correspond au code stocké, retourner true
        return storedCode.isNullOrEmpty() || code == storedCode
    }
}