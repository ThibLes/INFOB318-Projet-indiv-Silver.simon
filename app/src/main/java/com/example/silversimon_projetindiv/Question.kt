package com.example.silversimon_projetindiv

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.IOException
data class Name(val masculin: List<String>, val feminin: List<String>, val nonGenre: List<String>)


class Question (private val context: Context) {
    var names: Name? = null

    init {
        loadPrenoms()
    }

    private fun loadPrenoms() {
        try {
            context.assets.open("name").use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val json = String(buffer, Charsets.UTF_8)
                names = Gson().fromJson(json, Name::class.java)

                // Ajoutez des logs pour vérifier le contenu de prenoms après le chargement
                Log.d("QuestionGenerator", "Masculin: ${names?.masculin}")
                Log.d("QuestionGenerator", "Feminin: ${names?.feminin}")
                Log.d("QuestionGenerator", "Non-genre: ${names?.nonGenre}")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun generateQuestion(correctGenre: String, correctName: String): List<String> {
        // Assure-toi que le nom correct est dans la liste basée sur le genre
        val correctList = when (correctGenre) {
            "masculin" -> names?.masculin ?: listOf()
            "feminin" -> names?.feminin ?: listOf()
            else -> names?.nonGenre ?: listOf()
        }

        // Filtre pour s'assurer de ne pas inclure le nom correct dans la liste des autres noms
        val otherNames = correctList.filter { it != correctName }.shuffled().take(3)


        Log.d("Question", "Correct list size: ${correctList.size}")

        // Vérifie que nous avons suffisamment de noms pour les propositions
        if (correctList.size < 4) {
            // Peut-être retourner une liste par défaut ou afficher une erreur
            Log.e("Question", "Il n'y a pas assez de noms pour générer des propositions.")
            return listOf(correctName) // Retourne la liste qui contient uniquement le nom correct
        }

        // Ajoute le nom correct aux propositions
        return listOf(correctName) + otherNames
    }
}