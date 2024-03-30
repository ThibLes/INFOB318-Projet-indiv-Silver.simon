package com.example.silversimon_projetindiv

import android.content.Context
import com.google.gson.Gson
import java.io.IOException
data class Name(val male: List<String>, val feminin: List<String>, val nonGenre: List<String>)


/**
 * Manages the creation of questions based on masculine, feminine and non-gendered nouns.
 *
 * This class loads a list of names from a JSON file located in the assets (name.json),
 * and generates questions by randomly selecting names according to gender.
 *
 * @param context The application context, used to access assets.
 * @property names Contains lists of masculine, feminine and non-gendered names loaded from the JSON file.
 *
 * @author Thibaut Lesage
 *
 */

class Question (private val context: Context) {
    private var names: Name? = null

    // Load the names when the class is used
    init {
        loadPrenoms()
    }

    /**
     * Opens the `name.json` file, reads its contents, and retrieves all its data (the names)
     * converting the JSON into a [Name] object which is then assigned to [names].
     *
     * @throws IOException If the file cannot be opened or read.
     *
     * @author Thibaut Lesage
     * @version 1.4
     */
    private fun loadPrenoms() {
        try {
            // Open the file, look at its size, create a buffer of the same size (for temporary storage), read it and put everything in "name".
            context.assets.open("name").use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val json = String(buffer, Charsets.UTF_8)
                // Convert the JSON into a Name object
                names = Gson().fromJson(json, Name::class.java)
            }
        } catch (e: IOException) {
            // If the file cannot be opened or read, print the stack trace
            e.printStackTrace()
        }
    }

    /**
     * Generates a list of name proposals based on the correct gender and name (not to be repeated).
     *
     * This function selects the list of names with the same specified gender,
     * excludes the correct name so as not to have the same first name twice in the proposals
     * and makes a list of proposals with the correct name and three other random names.
     *
     * @param correctGender The gender for the proposals (masculine, feminine, or non-gendered).
     * @param correctName The correct name to put in the proposals.
     * @return A list of four names with the correct name and 3 other names of the same gender.
     *
     * @throws AssertionError If the names have not been loaded or the correct name is empty.
     *
     * @author Thibaut Lesage
     * @version 1.3
     */
    fun generateQuestion(correctGender: String, correctName: String): List<String> {
        assert(names != null) { "Les données des noms n'ont pas été chargées." }
        assert(correctName.isNotBlank()) { "Le nom correct ne peut pas être vide." }
        // Select the list of names according to the correct gender
        val correctList = when (correctGender) {
            "Homme" -> names?.male ?: listOf()
            "Femme" -> names?.feminin ?: listOf()
            else -> names?.nonGenre ?: listOf()
        }
        // Take 3 names at random from the list, checking that the first name is different from the correct one.
        val otherNames = correctList.filter { it != correctName }.shuffled().take(3)
        return listOf(correctName) + otherNames
    }
}