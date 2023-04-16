package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private lateinit var mealsFromDatabase: List<Meal>
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
                            mealsFromDatabase = mealDao.getMealsByName(mealToSearch)
                            updateUI(mealsFromDatabase)
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(mealsFromDatabase: List<Meal>) {
        if (mealsFromDatabase.isNotEmpty()) {
            runOnUiThread{
                mealInfo.text = buildString {
                    append("Found ")
                    append(mealsFromDatabase.size)
                    append(" meals for ")
                    append(mealTextInput.text)
                    append(".")
                }
                mealViewer.adapter = MealItemAdapter(this, mealsFromDatabase)
                mealViewer.setHasFixedSize(false)
            }
        } else{
            runOnUiThread{
                mealInfo.text = buildString {
                    append("No meals found for ")
                    append(mealTextInput.text)
                    append(".")
                }
                mealViewer.adapter = MealItemAdapter(this, mealsFromDatabase)
            }
        }
    }

    // update the screen when screen is rotated
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mealTextInput.text.toString().isNotEmpty() && mealsFromDatabase.isNotEmpty()) {
            outState.putString("mealTextInput", mealTextInput.text.toString())
            outState.putSerializable("mealsFromDatabase", mealsFromDatabase as ArrayList<Meal>)
        }
    }

    @Suppress("DEPRECATION")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("mealTextInput") && savedInstanceState.containsKey("mealsFromDatabase")){
            mealInfo.text = savedInstanceState.getString("mealInfo")
            mealsFromDatabase =
                savedInstanceState.getSerializable("mealsFromDatabase") as List<Meal>
            updateUI(mealsFromDatabase)
        }

    }
}