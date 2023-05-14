package com.example.meals

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    // Declaring variables
    private lateinit var mealNameTextInput: EditText
    private lateinit var searchBtn: Button
    private lateinit var mealInfo: TextView
    private lateinit var mealViewer: RecyclerView
    private lateinit var mealDetails: MutableList<Meal>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_by_name)
        // Initializing variables
        mealNameTextInput = findViewById(R.id.txtInputMealName)
        searchBtn = findViewById(R.id.btnSearchForMealsByName)
        mealViewer = findViewById(R.id.recycler_view)
        mealInfo = findViewById(R.id.txtMealNameInfo)

        // Searching for meals by name
        searchBtn.setOnClickListener{
            if (mealNameTextInput.text.toString().isEmpty()) {
                mealNameTextInput.error = "Please enter a meal"
            } else {
                mealInfo.text = getString(R.string.loading)
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
                                mealDetails = mutableListOf()
                                if (json.getString("meals").equals("null")) {
                                    runOnUiThread {
                                        mealInfo.text = buildString {
                                            append("No meals found for ")
                                            append(mealNameToSearch)
                                            append(".")
                                        }
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
                        mealInfo.text = buildString {
                            append("Error: ")
                            append(e.message.toString())
                        }
                    }
                }
            }
        }
    }

    // Update the UI with the meals found
    private fun updateUI(mealDetails: List<Meal>) {
        if (mealDetails.isNotEmpty()) {
            runOnUiThread {
                mealInfo.text = buildString {
                    append(mealDetails.size)
                    append(" meals found for ")
                    append(mealNameTextInput.text.toString())
                    append(".")
                }
                mealViewer.adapter = MealItemAdapter(this, mealDetails)
                mealViewer.setHasFixedSize(true)
            }
        } else {
            runOnUiThread {
                mealInfo.text = buildString {
                    append("No meals found for ")
                    append(mealNameTextInput.text.toString())
                    append(".")
                }
                mealViewer.adapter = MealItemAdapter(this, mealDetails)
                mealViewer.setHasFixedSize(true)
            }
        }
    }

    // save and update the screen when screen is rotated
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mealNameTextInput.text.toString().isNotEmpty() && mealInfo.text.toString().isNotEmpty() && mealDetails.isNotEmpty()){
            outState.putString("mealName", mealNameTextInput.text.toString())
            outState.putString("mealInfo", mealInfo.text.toString())
            outState.putSerializable("mealDetails", mealDetails as ArrayList<Meal>)
        }
    }

    @Suppress("DEPRECATION")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("mealName") && savedInstanceState.containsKey("mealInfo") && savedInstanceState.containsKey("mealDetails")) {
            mealNameTextInput.setText(savedInstanceState.getString("mealName"))
            mealInfo.text = savedInstanceState.getString("mealInfo")
            mealDetails = savedInstanceState.getSerializable("mealDetails") as MutableList<Meal>
            mealViewer.adapter = MealItemAdapter(this, mealDetails)
            mealViewer.setHasFixedSize(true)
        }
    }
}