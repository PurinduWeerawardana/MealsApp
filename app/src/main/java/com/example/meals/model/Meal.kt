package com.example.meals.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_table")
data class Meal (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val meal: String,
    val drinkAlternate: String?,
    val category: String,
    val area: String?,
    val instructions: String?,
    val mealThumb: String?,
    val tags: String?,
    val youtube: String?,
    val ingredients: String,
    val measures: String,
    val source: String?,
    val imageSource: String?,
    val creativeCommonsConfirmed: String?,
    val dateModified: String?){
}