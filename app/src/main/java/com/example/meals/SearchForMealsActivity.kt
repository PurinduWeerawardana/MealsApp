package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.room.Room
import com.example.meals.model.Meal
import com.example.meals.database.MealDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SearchForMealsActivity : AppCompatActivity() {
    private lateinit var mealTextInput: EditText
    private lateinit var searchMealsBtn: Button
    private lateinit var mealViewer: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_meals)
        mealTextInput = findViewById(R.id.txtInputMeal)
        searchMealsBtn = findViewById(R.id.btnSearchForMeals)
        mealViewer = findViewById(R.id.mealViewer)
        mealViewer.movementMethod = ScrollingMovementMethod()
        val db = Room.databaseBuilder(this, MealDatabase::class.java, "meal_database").build()
        val mealDao = db.mealDao()
        searchMealsBtn.setOnClickListener{
            if (mealTextInput.text.toString().isEmpty()) {
                mealTextInput.error = "Please enter a meal"
            } else {
                mealViewer.text = "Loading..."
                val mealToSearch = mealTextInput.text.toString()
                // get meal from database
                runBlocking {
                    launch {
                        withContext(Dispatchers.IO) {
                            val mealsFromDatabase: List<Meal> = mealDao.getMealsByName(mealToSearch)
                            if (mealsFromDatabase.isNotEmpty()) {
                                updateUI(mealsFromDatabase)
                            } else{
                                runOnUiThread{
                                    mealViewer.text = "No meals found for $mealToSearch."
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(mealsFromDatabase: List<Meal>) {
        val mealString = StringBuilder()
        for (meal in mealsFromDatabase) {
            mealString.append("Meal Name: ${meal.meal}\n\n")
            mealString.append("Meal Category: ${meal.category}\n\n")
            mealString.append("Meal Area: ${meal.area}\n\n")
            mealString.append("Meal Instructions: ${meal.instructions}\n\n")
            mealString.append("Meal Thumbnail: ${meal.mealThumb}\n\n")
            mealString.append("Meal Tags: ${meal.tags}\n\n")
            mealString.append("Meal YouTube Link: ${meal.youtube}\n\n")
            mealString.append("Meal Ingredients: ${meal.source}\n\n")
            mealString.append("Meal Image Source: ${meal.imageSource}\n\n")
            mealString.append("Meal Creative Commons: ${meal.creativeCommonsConfirmed}\n\n")
            mealString.append("Meal Date Modified: ${meal.dateModified}\n\n")
            mealString.append("Meal Ingredients: ${meal.ingredients}\n\n")
            mealString.append("Meal Measures: ${meal.measures}\n\n")
            mealString.append("\n\n")
        }
        runOnUiThread{
            mealViewer.text = mealString
        }

    }
}