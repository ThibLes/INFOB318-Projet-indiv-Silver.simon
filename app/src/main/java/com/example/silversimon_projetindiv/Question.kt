package com.example.silversimon_projetindiv

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.IOException
data class Name(val male: List<String>, val feminin: List<String>, val nonGenre: List<String>)


/**
 * Gère la création de questions basées sur des noms masculins, féminins et non-genrés.
 *
 * Cette classe charge une liste de noms depuis un fichier JSON situé dans les assets (name.json),
 * et génère des questions en sélectionnant aléatoirement des noms selon le genre.
 *
 * @param context Le contexte de l'application, utilisé pour accéder aux assets.
 * @property names Contient les listes de noms masculins, féminins et non-genrés chargées du fichier JSON.
 */
class Question (private val context: Context) {
    var names: Name? = null

    // Lance la fonction "loadPrenoms" dés que la class est utilisée.
    init {
        loadPrenoms()
    }

    /**
     * Cette fonction ouvre le fichier `name.json`, lit son contenu, et récupère toutes ses données ( les prénoms )
     * le JSON en un objet [Name] qui sera ensuite assigné à [names].
     */
    private fun loadPrenoms() {
        try {
            // Ouvre le fichier, regarde sa taille, créé un buffer de la même taille ( pour stocker temporairement ), le lit et mets tout dans "name"
            context.assets.open("name").use { inputStream ->
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val json = String(buffer, Charsets.UTF_8)
                names = Gson().fromJson(json, Name::class.java)
                Log.d("GameActivity", " $names")

            }
            // Vérifie que la liste de prénoms n'est pas vide
           // assert(names != null, { "Erreur lors de la lecture du fichier JSON, aucun prénom trouvé" })
        } catch (e: IOException) {
            // Print erreur en cas de problème
            e.printStackTrace()
        }
    }

    /**
     * Génère une liste de propositions de noms basée sur le genre et le nom corrects ( à ne pas repdnre ).
     *
     * Cette fonction sélectionne la liste des noms ayant le même genre spécifié,
     * exclut le nom correct pour pas avoir 2 fois le même prénom dans les propositions
     * et fait une liste de propositions avec le nom correct et trois autres noms aléatoires.
     *
     * @param correctGenre Le genre pour les propositions (masculin, féminin, ou non-genré).
     * @param correctName Le nom correct à mettre dans les propositions.
     * @return Une liste de quatre noms avec le nom correct et 3 autres noms du même genre.
     */
    fun generateQuestion(correctGenre: String, correctName: String): List<String> {
         // assert(names != null) { "Les données des noms n'ont pas été chargées." }
        // assert(correctName.isNotBlank()) { "Le nom correct ne peut pas être vide." }
        // Fait la bonne liste en fonction du bon genre
        val correctList = when (correctGenre) {
            "Homme" -> names?.male ?: listOf()
            "Femme" -> names?.feminin ?: listOf()
            else -> names?.nonGenre ?: listOf()
        }

        // Prend 3 prénoms au hasard dans la liste, en verifiant que le prénom est différent que le prénom correct
        val otherNames = correctList.filter { it != correctName }.shuffled().take(3)
        Log.d("GameActivity", " $otherNames")



        return listOf(correctName) + otherNames
    }
}