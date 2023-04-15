package com.example.meals.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.meals.model.Meal

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_table")
    fun getAllMeals(): List<Meal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMeals(List: List<Meal>)

    //get meal by name case insensitive
    @Query("SELECT * FROM meal_table WHERE meal LIKE '%' || :mealName || '%' OR ingredients LIKE '%' || :mealName || '%'")
    fun getMealsByName(mealName: String): List<Meal>
}