package com.mannsuvai.app

data class Recipe(
    val id: String = "",
    val name: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val imageUrl: String? = null
)
