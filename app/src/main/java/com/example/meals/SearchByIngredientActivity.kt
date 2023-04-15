package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.meals.adapter.MealItemAdapter
import com.example.meals.model.Meal
import com.example.meals.database.MealDatabase
import com.example.meals.utils.MealFromJSON
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
    private lateinit var mealInfo: TextView
    private lateinit var mealViewer: RecyclerView
    private val mealDetailsList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_ingredient)
        ingredientTextInput = findViewById(R.id.txtInputIngredient)
        retrieveMealsBtn = findViewById(R.id.btnRetrieveMeals)
        saveMealsBtn = findViewById(R.id.btnSaveMeals)
        mealInfo = findViewById(R.id.txtIngredientInfo)
        mealViewer = findViewById(R.id.recycler_view)
        retrieveMealsBtn.setOnClickListener {
            if (ingredientTextInput.text.toString().isEmpty()) {
                ingredientTextInput.error = "Please enter an ingredient"
            } else {
                mealInfo.text = "Loading..."
                val ingredient = ingredientTextInput.text.toString()
                var url = "https://www.themealdb.com/api/json/v1/1/filter.php?i=$ingredient"
                runBlocking{
                    launch {
                        withContext(Dispatchers.IO) {
                            try {
                                var urlConnection: HttpURLConnection  = URL(url).openConnection() as HttpURLConnection
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
                            } catch (e: Exception) {
                                runOnUiThread {
                                    mealInfo.text = "Error retrieving meals."
                                    Log.e("Error", e.toString())
                                }
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
                mealInfo.text = "No meals to save"
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
                mealInfo.text = "No meals found"
            }
        } else {
            val mealDetails: MutableList<Meal> = mutableListOf()
            for (eachMeal in mealDetailsList) {
                val json = JSONObject(eachMeal)
                val meals = json.getJSONArray("meals")
                for (i in 0 until meals.length()) {
                    val meal = meals.getJSONObject(i)
                    val newMeal: Meal = MealFromJSON.getMealFromJSON(meal)
                    mealDetails.add(newMeal)
                }
            }
            runOnUiThread {
                mealInfo.text = "${mealDetails.size} meals found for ${ingredientTextInput.text}"
                mealViewer.adapter = MealItemAdapter(this, mealDetails)
                mealViewer.setHasFixedSize(true)
            }
        }
    }
}