package com.mannsuvai.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * RecipeImageGenerator
 * Generates and manages recipe-related background images
 * Can use local generation or API integration for AI images
 */
object RecipeImageGenerator {

    /**
     * Generate a beautiful recipe background image locally
     * Uses gradients, shapes, and culinary patterns
     */
    fun generateRecipeBackground(recipeName: String, width: Int = 800, height: Int = 600): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Base gradient background
        drawGradientBackground(canvas, width, height)

        // Add culinary elements
        drawDecorativeElements(canvas, width, height, recipeName)

        // Add recipe name overlay
        drawRecipeName(canvas, width, height, recipeName)

        return bitmap
    }

    /**
     * Create gradient background with warm culinary colors
     */
    private fun drawGradientBackground(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Warm gradient from dark to warmer tone
        val colors = intArrayOf(
            Color.parseColor("#1A1A1A"),  // Dark background
            Color.parseColor("#2D2D2D"),
            Color.parseColor("#3D3D2D")
        )

        // Create gradient effect
        for (i in 0 until height) {
            val colorIndex = (i.toFloat() / height * (colors.size - 1)).toInt()
            val colorNext = if (colorIndex + 1 < colors.size) colors[colorIndex + 1] else colors[colorIndex]
            val currentColor = colors[colorIndex]

            paint.color = interpolateColor(currentColor, colorNext, (i % (height / colors.size)).toFloat() / (height / colors.size))
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), i.toFloat(), paint)
        }
    }

    /**
     * Add decorative culinary elements (spoons, leaves, etc.)
     */
    private fun drawDecorativeElements(canvas: Canvas, width: Int, height: Int, recipeName: String) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#D4A574")
            alpha = 30
            style = Paint.Style.FILL
        }

        // Draw spoon patterns
        drawSpoonPattern(canvas, width / 4, height / 3, paint)
        drawSpoonPattern(canvas, (width * 3) / 4, (height * 2) / 3, paint)

        // Draw leaf accents
        drawLeafAccent(canvas, width / 3, height / 4, paint)
        drawLeafAccent(canvas, (width * 2) / 3, (height * 3) / 4, paint)

        // Draw chili patterns
        drawChiliPattern(canvas, width / 2, height / 2, paint)
    }

    /**
     * Draw spoon decorative element
     */
    private fun drawSpoonPattern(canvas: Canvas, cx: Int, cy: Int, paint: Paint) {
        val radius = 40f

        // Spoon bowl
        canvas.drawOval(
            RectF(cx - radius, cy - radius, cx + radius, cy + radius),
            paint
        )

        // Spoon handle
        paint.strokeWidth = 8f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(
            cx.toFloat(), cy + radius,
            (cx + radius * 1.5).toFloat(), (cy + radius * 2.5).toFloat(),
            paint
        )
    }

    /**
     * Draw leaf accent pattern
     */
    private fun drawLeafAccent(canvas: Canvas, cx: Int, cy: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        val path = android.graphics.Path().apply {
            moveTo(cx.toFloat(), cy.toFloat())
            quadTo((cx + 20).toFloat(), (cy - 15).toFloat(), (cx + 25).toFloat(), cy.toFloat())
            quadTo((cx + 20).toFloat(), (cy + 15).toFloat(), cx.toFloat(), cy.toFloat())
        }
        canvas.drawPath(path, paint)
    }

    /**
     * Draw chili pepper pattern
     */
    private fun drawChiliPattern(canvas: Canvas, cx: Int, cy: Int, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#D4A574")
        paint.alpha = 20

        // Chili body
        canvas.drawOval(
            RectF((cx - 8).toFloat(), cy.toFloat(), (cx + 8).toFloat(), (cy + 30).toFloat()),
            paint
        )

        // Chili stem
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(cx.toFloat(), (cy + 30).toFloat(), cx.toFloat(), (cy + 40).toFloat(), paint)
    }

    /**
     * Draw recipe name on background
     */
    private fun drawRecipeName(canvas: Canvas, width: Int, height: Int, recipeName: String) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#D4A574")
            textSize = 48f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.SANS_SERIF,
                android.graphics.Typeface.BOLD
            )
        }

        // Limit recipe name length
        val displayName = if (recipeName.length > 30) {
            recipeName.substring(0, 27) + "..."
        } else {
            recipeName
        }

        // Draw recipe name at bottom with semi-transparent background
        val padding = 20f
        val textHeight = paint.descent() - paint.ascent()
        val rectTop = height - textHeight - padding * 2
        val rectBottom = height.toFloat()

        // Semi-transparent background for text
        val bgPaint = Paint().apply {
            color = Color.parseColor("#1A1A1A")
            alpha = 200
        }

        canvas.drawRect(0f, rectTop, width.toFloat(), rectBottom, bgPaint)

        // Draw text
        canvas.drawText(
            displayName,
            (width / 2).toFloat(),
            height - padding,
            paint
        )
    }

    /**
     * Interpolate between two colors
     */
    private fun interpolateColor(colorStart: Int, colorEnd: Int, amount: Float): Int {
        val aStart = Color.alpha(colorStart)
        val rStart = Color.red(colorStart)
        val gStart = Color.green(colorStart)
        val bStart = Color.blue(colorStart)

        val aEnd = Color.alpha(colorEnd)
        val rEnd = Color.red(colorEnd)
        val gEnd = Color.green(colorEnd)
        val bEnd = Color.blue(colorEnd)

        return Color.argb(
            (aStart + (aEnd - aStart) * amount).toInt(),
            (rStart + (rEnd - rStart) * amount).toInt(),
            (gStart + (gEnd - gStart) * amount).toInt(),
            (bStart + (bEnd - bStart) * amount).toInt()
        )
    }

    /**
     * Generate image using Anthropic API for AI-powered backgrounds
     * Uncomment and configure with your API key for AI image generation
     */
    suspend fun generateAIRecipeImage(recipeName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Example implementation (requires valid API key)
                val apiKey = BuildConfig.ANTHROPIC_API_KEY // Define in local.properties
                val prompt = "Create a beautiful, appetizing background image for a recipe called '$recipeName'. " +
                        "Use warm colors (golds, browns), culinary elements, and sophisticated design. " +
                        "The image should be 800x600 pixels and suitable as a background for a mobile app."

                // This is a placeholder - actual implementation would use HTTP client
                // val response = callAnthropicAPI(apiKey, prompt)
                // return@withContext response.imageUrl

                null // Return null until API is configured
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Resize bitmap to fit screen
     */
    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * Compress bitmap for efficient storage
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 85): ByteArray {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Cache bitmap locally for reuse
     */
    fun cacheBitmap(context: android.content.Context, bitmap: Bitmap, fileName: String) {
        val cacheDir = context.cacheDir
        val file = java.io.File(cacheDir, fileName)
        val fileOutputStream = java.io.FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOutputStream)
        fileOutputStream.close()
    }

    /**
     * Load cached bitmap
     */
    fun loadCachedBitmap(context: android.content.Context, fileName: String): Bitmap? {
        return try {
            val cacheDir = context.cacheDir
            val file = java.io.File(cacheDir, fileName)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * RecipeBackgroundManager
 * Manages recipe background images with caching strategy
 */
class RecipeBackgroundManager(private val context: android.content.Context) {

    /**
     * Get or create background for recipe
     */
    suspend fun getRecipeBackground(recipeName: String, forceRefresh: Boolean = false): Bitmap? {
        val cacheFileName = "recipe_bg_${recipeName.hashCode()}.jpg"

        // Try to load from cache first
        if (!forceRefresh) {
            RecipeImageGenerator.loadCachedBitmap(context, cacheFileName)?.let {
                return it
            }
        }

        // Generate new background
        val bitmap = RecipeImageGenerator.generateRecipeBackground(recipeName)

        // Cache for future use
        RecipeImageGenerator.cacheBitmap(context, bitmap, cacheFileName)

        return bitmap
    }

    /**
     * Clear all cached backgrounds
     */
    fun clearCache() {
        val cacheDir = context.cacheDir
        cacheDir.listFiles { file ->
            file.name.startsWith("recipe_bg_")
        }?.forEach { it.delete() }
    }
}
