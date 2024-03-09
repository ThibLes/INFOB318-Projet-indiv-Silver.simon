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

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun generateQuestion(correctGenre: String, correctName: String): List<String> {
        // A nom correct est dans la liste basée sur le genre
        val correctList = when (correctGenre) {
            "Homme" -> names?.masculin ?: listOf()
            "Femme" -> names?.feminin ?: listOf()
            else -> names?.nonGenre ?: listOf()
        }

        // Filtre pour s'assurer de ne pas inclure le nom correct dans la liste des autres noms
        val otherNames = correctList.filter { it != correctName }.shuffled().take(3)

        return listOf(correctName) + otherNames
    }
}