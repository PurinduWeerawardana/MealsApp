package com.example.meals.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
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
    class MealItemViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        val mealName: TextView = view.findViewById(R.id.mealName)
        val mealImage: ImageView = view.findViewById(R.id.mealImage)
        val mealCategory: TextView = view.findViewById(R.id.mealCategory)
        val mealArea: TextView = view.findViewById(R.id.mealArea)
        val mealTags: TextView = view.findViewById(R.id.mealTags)
        val mealDrinkAlternate: TextView = view.findViewById(R.id.mealDrinkAlternate)
        val ingredientsTableLayout: TableLayout = view.findViewById(R.id.ingredientsAndMeasuresTable)
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
        holder.mealCategory.text = buildString {
            append("Category: ")
            append(meal.category)
        }
        holder.mealArea.text = buildString {
            append("Area: ")
            append(meal.area)
        }
        holder.mealTags.text = buildString {
            append("Tags: ")
            append(meal.tags)
        }
        holder.mealDrinkAlternate.text = buildString {
            append("Drink Alternate: ")
            append(meal.drinkAlternate)
        }
        val ingredientsList: List<String> = meal.ingredients.split(",")
        val measuresList: List<String> = meal.measures.split(",")
        holder.ingredientsTableLayout.removeAllViews()
        val headerRow = TableRow(context)
        val headerIngredient = TextView(context)
        val headerMeasure = TextView(context)
        headerIngredient.text = context.getString(R.string.ingredientCap)
        headerIngredient.textSize = 16f
        headerIngredient.setTypeface(null, Typeface.BOLD)
        headerIngredient.setPadding(30, 0, 30, 0)
        headerIngredient.textAlignment = View.TEXT_ALIGNMENT_CENTER
        headerIngredient.setBackgroundResource(R.drawable.border)
        headerMeasure.text = context.getString(R.string.neasureCap)
        headerMeasure.textSize = 16f
        headerMeasure.setTypeface(null, Typeface.BOLD)
        headerMeasure.setPadding(30, 0, 30, 0)
        headerMeasure.textAlignment = View.TEXT_ALIGNMENT_CENTER
        headerMeasure.setBackgroundResource(R.drawable.border)
        headerRow.addView(headerIngredient)
        headerRow.addView(headerMeasure)
        holder.ingredientsTableLayout.addView(headerRow)
        for (i in ingredientsList) {
            if (i != "null") {
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
        holder.mealVideo.text = buildString {
            append("Video: ")
            append(meal.youtube)
        }
    }
}