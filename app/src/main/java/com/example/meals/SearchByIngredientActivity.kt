package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.example.meals.model.Meal
import com.example.meals.database.MealDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class SearchByIngredientActivity : AppCompatActivity() {
    private lateinit var ingredientTextInput: EditText
    private lateinit var retrieveMealsBtn: Button
    private lateinit var saveMealsBtn: Button
    private lateinit var recipeViewer: TextView
    private val mealDetailsList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_ingredient)
        ingredientTextInput = findViewById(R.id.txtInputIngredient)
        retrieveMealsBtn = findViewById(R.id.btnRetrieveMeals)
        saveMealsBtn = findViewById(R.id.btnSaveMeals)
        recipeViewer = findViewById(R.id.recipeViewer)
        recipeViewer.movementMethod = ScrollingMovementMethod()
        retrieveMealsBtn.setOnClickListener {
            if (ingredientTextInput.text.toString().isEmpty()) {
                ingredientTextInput.error = "Please enter an ingredient"
            } else {
                recipeViewer.text = "Loading..."
                val ingredient = ingredientTextInput.text.toString()
                var url = "https://www.themealdb.com/api/json/v1/1/filter.php?i=$ingredient"
                var urlConnection: HttpURLConnection  = URL(url).openConnection() as HttpURLConnection
                runBlocking{
                    launch {
                        withContext(Dispatchers.IO) {
                            val responseMeals = urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)
                            urlConnection.disconnect()
                            val json = JSONObject(responseMeals)
                            mealDetailsList.clear()
                            if (json.getString("meals").equals("null")) {
                                updateUI(mutableListOf("No meals found for $ingredient."),false)
                            } else{
                                val meals = json.getJSONArray("meals")
                                val mealIds = mutableListOf<String>()
                                for (i in 0 until meals.length()) {
                                    val meal = meals.getJSONObject(i)
                                    mealIds.add(meal.getString("idMeal"))
                                }
                                for (id in mealIds) {
                                    url = "https://www.themealdb.com/api/json/v1/1/lookup.php?i=$id"
                                    urlConnection = URL(url).openConnection() as HttpURLConnection
                                    val responseMealsDetails = urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)
                                    mealDetailsList.add(responseMealsDetails)
                                    urlConnection.disconnect()
                                }
                                updateUI(mealDetailsList)
                            }
                        }
                    }
                }
            }
        }

        val db = Room.databaseBuilder(this, MealDatabase::class.java, "meal_database").build()
        val mealDao = db.mealDao()
        saveMealsBtn.setOnClickListener{
            if (mealDetailsList.isEmpty()) {
                recipeViewer.text = "No meals to save"
            } else {
                val meals = mutableListOf<Meal>()
                for (eachMeal in mealDetailsList){
                    val json = JSONObject(eachMeal)
                    val mealsArray = json.getJSONArray("meals")
                    for (i in 0 until mealsArray.length()) {
                        val meal = mealsArray.getJSONObject(i)
                        val mealId = meal.getString("idMeal")
                        val mealName = meal.getString("strMeal")
                        val mealCategory = meal.getString("strCategory")
                        val mealArea = meal.getString("strArea")
                        val mealInstructions = meal.getString("strInstructions")
                        val mealThumb = meal.getString("strMealThumb")
                        val mealTags = meal.getString("strTags")
                        val mealYoutube = meal.getString("strYoutube")
                        val mealSource = meal.getString("strSource")
                        val mealImageSource = meal.getString("strImageSource")
                        val mealIngredients = mutableListOf<String>()
                        val mealMeasures = mutableListOf<String>()
                        for (j in 1..20) {
                            val ingredient = meal.getString("strIngredient$j")
                            if (ingredient.isNotEmpty()) {
                                val measure = meal.getString("strMeasure$j")
                                mealIngredients.add(ingredient)
                                mealMeasures.add(measure)
                            }
                        }
                        val mealDrinkAlternate = meal.getString("strDrinkAlternate")
                        val mealCreativeCommonsConfirmed = meal.getString("strCreativeCommonsConfirmed")
                        val mealDateModified = meal.getString("dateModified")
                        meals.add(Meal(mealId.toInt(),mealName,mealDrinkAlternate,mealCategory,mealArea,mealInstructions,mealThumb,mealTags,mealYoutube,mealIngredients.joinToString(","),mealMeasures.joinToString(","),mealSource,mealImageSource,mealCreativeCommonsConfirmed,mealDateModified))
                    }
                    runBlocking {
                        launch {
                            withContext(Dispatchers.IO) {
                                try {
                                    mealDao.insertMeals(meals)
                                    runOnUiThread {
                                        Toast.makeText(this@SearchByIngredientActivity, "Meals saved successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    runOnUiThread {
                                        Toast.makeText(this@SearchByIngredientActivity, "Error saving meals", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    private fun updateUI(mealDetailsList: MutableList<String>,mealsFound: Boolean = true) {
        if (!mealsFound) {
            runOnUiThread {
                recipeViewer.text = "No meals found"
            }
            return
        }
        val mealsDetails = StringBuilder()
        for (eachMeal in mealDetailsList){
            val json = JSONObject(eachMeal)
            val meals = json.getJSONArray("meals")
            val mealString = StringBuilder()
            for (i in 0 until meals.length()) {
                val meal = meals.getJSONObject(i)
                mealString.append("Meal Name: ${meal.getString("strMeal")}\n\n")
                mealString.append("Meal Category: ${meal.getString("strCategory")}\n\n")
                mealString.append("Meal Area: ${meal.getString("strArea")}\n\n")
                mealString.append("Meal Instructions: ${meal.getString("strInstructions")}\n\n")
                mealString.append("Meal Thumbnail: ${meal.getString("strMealThumb")}\n\n")
                mealString.append("Meal Tags: ${meal.getString("strTags")}\n\n")
                mealString.append("Meal Youtube Link: ${meal.getString("strYoutube")}\n\n")
                mealString.append("Meal Source: ${meal.getString("strSource")}\n\n")
                mealString.append("Meal Image Source: ${meal.getString("strImageSource")}\n\n")
                mealString.append("Meal Creative Commons Confirmed: ${meal.getString("strCreativeCommonsConfirmed")}\n\n")
                mealString.append("Meal Date Modified: ${meal.getString("dateModified")}\n\n")
                mealString.append("Meal Ingredients: \n")
                for (j in 1..20) {
                    val ingredient = meal.getString("strIngredient$j")
                    if (!ingredient.isNullOrBlank()) {
                        mealString.append("\t\tIngredient $j: $ingredient\n")
                    }
                }
                mealString.append("\n\nMeal Measures: \n")
                for (j in 1..20) {
                    val measure = meal.getString("strMeasure$j")
                    if (!measure.isNullOrBlank()) {
                        mealString.append("\t\tMeasure $j: $measure\n")
                    }
                }
                mealString.append("\n")
            }
            mealsDetails.append(mealString)
            mealsDetails.append("\n")
        }
        runOnUiThread {
            recipeViewer.text = mealsDetails
        }

    }
}