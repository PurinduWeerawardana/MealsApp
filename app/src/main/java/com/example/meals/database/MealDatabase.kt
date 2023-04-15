package com.example.meals.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.meals.model.Meal

@Database(entities = [Meal::class], version = 2)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
