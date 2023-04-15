package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class SearchByNameActivity : AppCompatActivity() {
    private lateinit var mealNameTextInput: EditText
    private lateinit var searchBtn: Button
    private lateinit var recipeViewer: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_name)
        mealNameTextInput = findViewById(R.id.txtInputMealName)
        searchBtn = findViewById(R.id.btnSearchForMealsByName)
        recipeViewer = findViewById(R.id.mealsViewer)
        recipeViewer.movementMethod = ScrollingMovementMethod()
        searchBtn.setOnClickListener{
            if (mealNameTextInput.text.toString().isEmpty()) {
                mealNameTextInput.error = "Please enter a meal"
            } else {
                recipeViewer.text = "Loading..."
                val mealNameToSearch = mealNameTextInput.text.toString()
                // get meals from themealdb api
                val url = "https://www.themealdb.com/api/json/v1/1/search.php?s=$mealNameToSearch"
                val urlConnection: HttpURLConnection  = URL(url).openConnection() as HttpURLConnection
                runBlocking{
                    launch {
                        withContext(Dispatchers.IO) {
                            val responseMeals = urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)
                            urlConnection.disconnect()
                            val json = JSONObject(responseMeals)
                            val mealDetails = StringBuilder()
                            if (json.getString("meals").equals("null")) {
                                mealDetails.append("No meals found for $mealNameToSearch.")
                                updateUI(mealDetails)
                            } else {
                                val meals = json.getJSONArray("meals")
                                for (i in 0 until meals.length()) {
                                    val meal = meals.getJSONObject(i)
                                    mealDetails.append("Meal Name: ${meal.getString("strMeal")}\n\n")
                                    mealDetails.append("Meal Category: ${meal.getString("strCategory")}\n\n")
                                    mealDetails.append("Meal Area: ${meal.getString("strArea")}\n\n")
                                    mealDetails.append("Meal Instructions: ${meal.getString("strInstructions")}\n\n")
                                    mealDetails.append("Meal Thumbnail: ${meal.getString("strMealThumb")}\n\n")
                                    mealDetails.append("Meal Tags: ${meal.getString("strTags")}\n\n")
                                    mealDetails.append("Meal Youtube Link: ${meal.getString("strYoutube")}\n\n")
                                    mealDetails.append("Meal Source: ${meal.getString("strSource")}\n\n")
                                    mealDetails.append("Meal Image Source: ${meal.getString("strImageSource")}\n\n")
                                    mealDetails.append(
                                        "Meal Creative Commons Confirmed: ${
                                            meal.getString(
                                                "strCreativeCommonsConfirmed"
                                            )
                                        }\n\n"
                                    )
                                    mealDetails.append("Meal Date Modified: ${meal.getString("dateModified")}\n\n")
                                    mealDetails.append("Meal Ingredients: \n")
                                    for (j in 1..20) {
                                        val ingredient = meal.getString("strIngredient$j")
                                        if (!ingredient.isNullOrBlank()) {
                                            mealDetails.append("\t\tIngredient $j: $ingredient\n")
                                        }
                                    }
                                    mealDetails.append("\n\nMeal Measures: \n")
                                    for (j in 1..20) {
                                        val measure = meal.getString("strMeasure$j")
                                        if (!measure.isNullOrBlank()) {
                                            mealDetails.append("\t\tMeasure $j: $measure\n")
                                        }
                                    }
                                    mealDetails.append("\n")
                                }
                                updateUI(mealDetails)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(mealDetails: StringBuilder) {
        runOnUiThread {
            recipeViewer.text = mealDetails
        }
    }
}