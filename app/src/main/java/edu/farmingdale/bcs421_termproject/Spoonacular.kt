package edu.farmingdale.bcs421_termproject

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Represents Spoonacular recipes retrieved from an external API.
 */
class Spoonacular(
    val id: Int,
    val title: String,
    val description: String,
    var sourceUrl: String,
    var image: String,
    var readyInMinutes: Int,
    var servings: Int,
    var nutrition: Nutrition
) {
    companion object {
        private const val apiKey = "apiKey=7089afb084ac4235a7014fa58ba91a18"
        private val recipeList = mutableListOf<Spoonacular>()


        suspend fun getApiResponse(apiUrl: String): JSONObject? = withContext(Dispatchers.IO) {
            try {
                // Create a URL object and open a connection
                val url = URL(apiUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                // Get the HTTP response code
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read and parse the JSON response
                    val inStream = BufferedReader(InputStreamReader(connection.inputStream))
                    var inputLine: String?
                    val response = StringBuilder()

                    while (inStream.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    inStream.close()

                    // Parse the JSON response
                    return@withContext JSONObject(response.toString())
                } else {
                    println("Request failed with response code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext null // Return null in case of an error
        }

        private fun parseRecipes(jsonResponse: JSONObject?): List<Spoonacular> {
            val recipes = mutableListOf<Spoonacular>()

            // Check if the JSON response is not null
            if (jsonResponse != null) {
                try {
                    // Get the "results" array from the JSON response
                    val results = jsonResponse.getJSONArray("results")

                    // Iterate through each recipe in the results array
                    for (i in 0 until results.length()) {
                        val recipeObject = results.getJSONObject(i)

                        // Extract relevant information from the recipe object
                        val id = recipeObject.optInt("id", 1)
                        val title = recipeObject.optString("title", "Unknown Title")
                        val description = recipeObject.optString("summary", "Description Unavailable")
                        val sourceUrl = recipeObject.optString("sourceUrl", "Source Unavailable")
                        val image = recipeObject.optString("image", "Unknown Image")
                        val readyInMinutes = recipeObject.optInt("readyInMinutes", 0)
                        val servings = recipeObject.optInt("servings", 1)

                        // Extract nutritional information
                        val nutritionObject = recipeObject.optJSONObject("nutrition")
                        val nutrientsArray = nutritionObject?.optJSONArray("nutrients")

                        val calories = nutrientsArray?.getJSONObject(0)?.optDouble("amount") ?: 0.0
                        val fat = nutrientsArray?.getJSONObject(1)?.optDouble("amount") ?: 0.0
                        val carbohydrates = nutrientsArray?.getJSONObject(3)?.optDouble("amount") ?: 0.0
                        val protein = nutrientsArray?.getJSONObject(9)?.optDouble("amount") ?: 0.0

                        // Create a Nutrition object
                        val nutrition = Nutrition(calories, carbohydrates, protein, fat)

                        // Create a Spoonacular object and add it to the list
                        recipes.add(Spoonacular(id, title, description, sourceUrl, image, readyInMinutes, servings, nutrition))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Log.d("API_RESPONSE", "API response: $recipes")
            return recipes
        }

        suspend fun searchRecipes(query: String): List<Spoonacular> = withContext(Dispatchers.IO)  {
            val apiUrl = "https://api.spoonacular.com/recipes/complexSearch?query=$query&addRecipeNutrition=true&$apiKey"

            try {
                // Make the API call to get recipes based on the search query
                val jsonResponse = getApiResponse(apiUrl)

                // Parse the JSON response and return a list of recipes
                return@withContext parseRecipes(jsonResponse)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext emptyList() // Return an empty list in case of an error
        }

        suspend fun getRandRecipe(): JSONObject? = withContext(Dispatchers.IO) {
            val randNum = Random.nextInt(0, 715391).toString()
            // Define the API URL for Spoonacular recipes
            val apiUrl = "https://api.spoonacular.com/recipes/$randNum/information?includeNutrition=true&$apiKey"

            try {
                // Create a URL object and open a connection
                val url = URL(apiUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                // Get the HTTP response code
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read and parse the JSON response
                    val inStream = BufferedReader(InputStreamReader(connection.inputStream))
                    var inputLine: String?
                    val response = StringBuilder()

                    while (inStream.readLine().also { inputLine = it } != null) {
                        response.append(inputLine)
                    }
                    inStream.close()

                    // Parse the JSON response directly as a JSONObject
                    return@withContext JSONObject(response.toString())
                } else {
                    println("Request failed with response code: $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext null // Return null in case of an error or no recipes
        }


        // Get n random recipes
        suspend fun getRandRecipes(numRecipes: Int): List<Spoonacular> {
            val recipes = mutableListOf<Spoonacular>()

            repeat(numRecipes) {
                getRandRecipe()?.let { jsonObject ->

                    try {
                        val id = jsonObject.getInt("id")
                        val title = jsonObject.optString("title", "Temp Title")
                        val description = jsonObject.optString("summary", "Description Unavailable: Please visit source link")
                        val sourceUrl = jsonObject.getString("sourceUrl")
                        val image = jsonObject.optString("image", "null")
                        val readyInMinutes = jsonObject.getInt("readyInMinutes")
                        val servings = jsonObject.getInt("servings")

                        // Extract nutritional information
                        val nutritionObject = jsonObject.optJSONObject("nutrition")
                        val nutrientsArray = nutritionObject?.optJSONArray("nutrients")

                        val calories = nutrientsArray?.getJSONObject(0)?.optDouble("amount") ?: 0.0
                        val fat = nutrientsArray?.getJSONObject(1)?.optDouble("amount") ?: 0.0
                        val carbohydrates = nutrientsArray?.getJSONObject(3)?.optDouble("amount") ?: 0.0
                        val protein = nutrientsArray?.getJSONObject(9)?.optDouble("amount") ?: 0.0

                        // Create a Nutrition object
                        val nutrition = Nutrition(calories, carbohydrates, protein, fat)

                        recipes.add(Spoonacular(id, title, description, sourceUrl, image, readyInMinutes, servings, nutrition))
                    } catch (e: JSONException) {
                        println("Error parsing JSON: ${e.message}")
                    }
                }
            }

            return recipes
        }
    }

    // Create a Nutrition class to store nutrition information
    data class Nutrition(
        val calories: Double,
        val carbohydrates: Double,
        val protein: Double,
        val fat: Double
    )
}