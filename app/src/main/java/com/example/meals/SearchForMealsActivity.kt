package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.meals.adapter.MealItemAdapter
import com.example.meals.model.Meal
import com.example.meals.database.MealDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SearchForMealsActivity : AppCompatActivity() {
    private lateinit var mealTextInput: EditText
    private lateinit var searchMealsBtn: Button
    private lateinit var mealViewer: RecyclerView
    private lateinit var mealInfo: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_for_meals)
        mealTextInput = findViewById(R.id.txtInputMeal)
        searchMealsBtn = findViewById(R.id.btnSearchForMeals)
        mealViewer = findViewById(R.id.recycler_view)
        mealInfo = findViewById(R.id.txtMealInfo)
        val db = Room.databaseBuilder(this, MealDatabase::class.java, "meal_database").build()
        val mealDao = db.mealDao()
        searchMealsBtn.setOnClickListener{
            if (mealTextInput.text.toString().isEmpty()) {
                mealTextInput.error = "Please enter a meal"
            } else {
                val mealToSearch = mealTextInput.text.toString()
                runBlocking {
                    launch {
                        withContext(Dispatchers.IO) {
                            val mealsFromDatabase: List<Meal> = mealDao.getMealsByName(mealToSearch)
                            if (mealsFromDatabase.isNotEmpty()) {
                                updateUI(mealsFromDatabase)
                            } else{
                                runOnUiThread{
                                    mealInfo.text = "No meals found for $mealToSearch."
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(mealsFromDatabase: List<Meal>) {
        runOnUiThread{
            mealInfo.text = "Found ${mealsFromDatabase.size} meals for ${mealTextInput.text}."
            mealViewer.adapter = MealItemAdapter(this, mealsFromDatabase)
            mealViewer.setHasFixedSize(false)
        }
    }
}