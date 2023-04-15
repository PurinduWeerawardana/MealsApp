package com.example.meals.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.meals.R
import com.example.meals.model.Meal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL

class MealItemAdapter(private val context: Context, private val meals: List<Meal>): RecyclerView.Adapter<MealItemAdapter.MealItemViewHolder>() {
    class MealItemViewHolder (private val view: View) : RecyclerView.ViewHolder(view) {
        val mealName: TextView = view.findViewById(R.id.mealName)
        val mealImage: ImageView = view.findViewById(R.id.mealImage)
        val mealCategory: TextView = view.findViewById(R.id.mealCategory)
        val mealArea: TextView = view.findViewById(R.id.mealArea)
        val mealTags: TextView = view.findViewById(R.id.mealTags)
        val mealDrinkAlternate: TextView = view.findViewById(R.id.mealDrinkAlternate)
        val ingredientsTableLayout: TableLayout = view.findViewById(R.id.ingredientsAndMeasuresTable)
//        val mealIngredients: TextView = view.findViewById(R.id.mealIngredients)
//        val mealMeasures: TextView = view.findViewById(R.id.mealMeasures)
        val mealInstructions: TextView = view.findViewById(R.id.mealInstructions)
        val mealVideo: TextView = view.findViewById(R.id.mealVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return MealItemViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {
        return meals.size
    }

    override fun onBindViewHolder(holder: MealItemViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealName.text = meal.meal
        runBlocking {
            launch{
                withContext(Dispatchers.IO){
                    val bmp: Bitmap = BitmapFactory.decodeStream(URL(meal.mealThumb).openConnection().getInputStream())
                    holder.mealImage.setImageBitmap(bmp)
                }
            }
        }
        holder.mealCategory.text = "Category: ${meal.category}"
        holder.mealArea.text = "Area: ${meal.area}"
        holder.mealTags.text = "Tags: ${meal.tags}"
        holder.mealDrinkAlternate.text = "Drink Alternate: ${meal.drinkAlternate}"
        val ingredientsList: List<String> = meal.ingredients.split(",")
        val measuresList: List<String> = meal.measures.split(",")
        for (i in ingredientsList) {
            if (!i.equals("null")){
                val ingredientRow = TableRow(context)
                val ingredientName = TextView(context)
                val ingredientMeasure = TextView(context)
                ingredientName.text = i
                ingredientName.setPadding(30, 0, 30, 0)
                ingredientName.textAlignment = View.TEXT_ALIGNMENT_CENTER
                ingredientName.setBackgroundResource(R.drawable.border)
                ingredientMeasure.text = measuresList[ingredientsList.indexOf(i)]
                ingredientMeasure.setPadding(30, 0, 30, 0)
                ingredientMeasure.textAlignment = View.TEXT_ALIGNMENT_CENTER
                ingredientMeasure.setBackgroundResource(R.drawable.border)
                ingredientRow.addView(ingredientName)
                ingredientRow.addView(ingredientMeasure)
                holder.ingredientsTableLayout.addView(ingredientRow)
            }
        }
//        holder.mealIngredients.text = "Ingredients: ${meal.ingredients}"
//        holder.mealMeasures.text = "Measures: ${meal.measures}"
        holder.mealInstructions.text = "${meal.instructions}"
        holder.mealVideo.text = "Video: ${meal.youtube}"
    }
}