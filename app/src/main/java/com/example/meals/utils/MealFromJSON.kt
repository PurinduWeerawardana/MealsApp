package com.example.meals.utils

import com.example.meals.model.Meal
import org.json.JSONObject

class MealFromJSON {
    companion object{
        fun getMealFromJSON(mealJSON: JSONObject) : Meal {
            val ingredients: MutableList<String> = mutableListOf()
            val measures: MutableList<String> = mutableListOf()
            for (j in 1..20) {
                val ingredient: String = mealJSON.getString("strIngredient$j")
                val measure: String = mealJSON.getString("strMeasure$j")
                if (ingredient.isNotBlank()) {
                    ingredients.add(ingredient)
                    measures.add(measure)
                } else {
                    break
                }
            }
            return Meal(
                mealJSON.getString("idMeal").toInt(),
                mealJSON.getString("strMeal"),
                mealJSON.getString("strDrinkAlternate"),
                mealJSON.getString("strCategory"),
                mealJSON.getString("strArea"),
                mealJSON.getString("strInstructions"),
                mealJSON.getString("strMealThumb"),
                mealJSON.getString("strTags"),
                mealJSON.getString("strYoutube"),
                ingredients.joinToString(","),
                measures.joinToString(","),
                mealJSON.getString("strSource"),
                mealJSON.getString("strImageSource"),
                mealJSON.getString("strCreativeCommonsConfirmed"),
                mealJSON.getString("dateModified")
            )
        }
    }
}