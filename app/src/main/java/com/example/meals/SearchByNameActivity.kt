package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meals.adapter.MealItemAdapter
import com.example.meals.model.Meal
import com.example.meals.utils.MealFromJSON
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
    private lateinit var mealInfo: TextView
    private lateinit var mealViewer: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_name)
        mealNameTextInput = findViewById(R.id.txtInputMealName)
        searchBtn = findViewById(R.id.btnSearchForMealsByName)
        mealViewer = findViewById(R.id.recycler_view)
        mealInfo = findViewById(R.id.txtMealNameInfo)
        searchBtn.setOnClickListener{
            if (mealNameTextInput.text.toString().isEmpty()) {
                mealNameTextInput.error = "Please enter a meal"
            } else {
                mealInfo.text = "Loading..."
                val mealNameToSearch = mealNameTextInput.text.toString()
                try {
                    val url = "https://www.themealdb.com/api/json/v1/1/search.php?s=$mealNameToSearch"
                    val urlConnection: HttpURLConnection  = URL(url).openConnection() as HttpURLConnection
                    runBlocking{
                        launch {
                            withContext(Dispatchers.IO) {
                                val responseMeals = urlConnection.inputStream.bufferedReader().use(BufferedReader::readText)
                                urlConnection.disconnect()
                                val json = JSONObject(responseMeals)
                                val mealDetails: MutableList<Meal> = mutableListOf()
                                if (json.getString("meals").equals("null")) {
                                    runOnUiThread {
                                        mealInfo.text = "No meals found for $mealNameToSearch."
                                    }
                                } else {
                                    val meals = json.getJSONArray("meals")
                                    for (i in 0 until meals.length()) {
                                        val meal: JSONObject = meals.getJSONObject(i)
                                        val newMeal: Meal = MealFromJSON.getMealFromJSON(meal)
                                        mealDetails.add(newMeal)
                                    }
                                updateUI(mealDetails)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Error", e.message.toString())
                    runOnUiThread{
                        mealInfo.text = "Error: ${e.message.toString()}"
                    }
                }
            }
        }
    }

    private fun updateUI(mealDetails: List<Meal>) {
        if (mealDetails.isNotEmpty()) {
            runOnUiThread {
                mealInfo.text = "${mealDetails.size} meals found for ${mealNameTextInput.text.toString()}."
                mealViewer.adapter = MealItemAdapter(this, mealDetails)
                mealViewer.setHasFixedSize(true)
            }
        } else {
            runOnUiThread {
                mealInfo.text = "No meals found for ${mealNameTextInput.text.toString()}."
                mealViewer.adapter = MealItemAdapter(this, mealDetails)
                mealViewer.setHasFixedSize(true)
            }
        }
    }
}