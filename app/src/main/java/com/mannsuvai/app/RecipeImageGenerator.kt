package com.mannsuvai.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

/**
 * RecipeImageGenerator
 * Generates custom shareable images for recipes
 */
object RecipeImageGenerator {

    fun generateRecipeImage(context: Context, recipe: Recipe): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background color
        canvas.drawColor(Color.parseColor("#1E1E1E"))

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            isAntiAlias = true
        }

        // Title
        canvas.drawText(recipe.name, 100f, 200f, paint)

        // Footer text
        paint.textSize = 32f
        paint.color = Color.GRAY
        canvas.drawText("Mann Suvai", 100f, height - 100f, paint)

        return bitmap
    }
}
