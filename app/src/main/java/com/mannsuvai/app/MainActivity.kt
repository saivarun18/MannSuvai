package com.mannsuvai.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class Recipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val steps: List<String>,
    val backgroundUrl: String = ""
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MannSuvaiApp()
        }
    }
}

@Composable
fun MannSuvaiApp() {
    val context = LocalContext.current
    var recipes by remember { mutableStateOf(listOf<Recipe>()) }
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var showCamera by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            extractRecipeFromImage(bitmap) { extractedSteps, recipeName ->
                val newRecipe = Recipe(
                    name = recipeName,
                    steps = extractedSteps
                )
                recipes = recipes + newRecipe
                showCamera = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                extractRecipeFromImage(bitmap) { extractedSteps, recipeName ->
                    val newRecipe = Recipe(
                        name = recipeName,
                        steps = extractedSteps
                    )
                    recipes = recipes + newRecipe
                    showCamera = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFD4A574),
            secondary = Color(0xFF8B6F47),
            background = Color(0xFF1A1A1A),
            surface = Color(0xFF2D2D2D)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                selectedRecipe != null -> {
                    RecipeDetailScreen(
                        recipe = selectedRecipe!!,
                        onBack = { selectedRecipe = null },
                        onShare = { shareRecipe(context, selectedRecipe!!) }
                    )
                }
                showCamera -> {
                    CameraInputScreen(
                        onCameraClick = {
                            val permission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permission == PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(null)
                            }
                        },
                        onGalleryClick = { galleryLauncher.launch("image/*") },
                        onBack = { showCamera = false }
                    )
                }
                else -> {
                    RecipeListScreen(
                        recipes = recipes,
                        onRecipeClick = { selectedRecipe = it },
                        onAddRecipe = { showCamera = true }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onAddRecipe: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Mann Suvai",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD4A574)
            )
            FloatingActionButton(
                onClick = onAddRecipe,
                containerColor = Color(0xFFD4A574),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }

        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp),
                        tint = Color(0xFF8B6F47)
                    )
                    Text(
                        "No recipes yet",
                        fontSize = 18.sp,
                        color = Color(0xFF8B8B8B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Photograph your handwritten recipes to get started",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = onAddRecipe,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD4A574)
                        )
                    ) {
                        Text("Add First Recipe", color = Color.Black)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recipes) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    recipe.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4A574),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "${recipe.steps.size} steps",
                    fontSize = 14.sp,
                    color = Color(0xFF8B8B8B)
                )
            }
            Icon(
                Icons.Default.Camera,
                contentDescription = null,
                tint = Color(0xFF8B6F47),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun CameraInputScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Capture Your Recipe",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD4A574),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onCameraClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD4A574)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Camera,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                tint = Color.Black
            )
            Text("Take Photo", color = Color.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onGalleryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF8B6F47)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Choose from Gallery", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3D3D3D)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back", color = Color(0xFFD4A574), fontSize = 16.sp)
        }
    }
}

@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            ) {
                Text("← Back", color = Color(0xFFD4A574))
            }
            
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4A574)
                )
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share", color = Color.Black)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    recipe.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD4A574),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(recipe.steps.size) { index ->
                StepCard(step = recipe.steps[index], stepNumber = index + 1)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StepCard(step: String, stepNumber: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D2D2D)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 12.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFD4A574)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "$stepNumber",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
            
            Text(
                step,
                fontSize = 16.sp,
                color = Color(0xFFE0E0E0),
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 24.sp
            )
        }
    }
}

fun extractRecipeFromImage(
    bitmap: Bitmap,
    onExtracted: (steps: List<String>, recipeName: String) -> Unit
) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            val extractedText = visionText.text
            val steps = parseRecipeSteps(extractedText)
            val recipeName = extractRecipeName(extractedText)
            onExtracted(steps, recipeName)
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}

fun parseRecipeSteps(text: String): List<String> {
    val lines = text.split("\n").filter { it.trim().isNotEmpty() }
    
    return lines.mapNotNull { line ->
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> null
            trimmed.matches(Regex("^[0-9]+[.)\\-].*")) -> {
                trimmed.replaceFirst(Regex("^[0-9]+[.)\\-]\\s*"), "").trim()
            }
            trimmed.matches(Regex("^[•\\-*].*")) -> {
                trimmed.replaceFirst(Regex("^[•\\-*]\\s*"), "").trim()
            }
            else -> trimmed
        }
    }.filter { it.length > 3 }
}

fun extractRecipeName(text: String): String {
    val firstLine = text.split("\n").firstOrNull { it.trim().isNotEmpty() } ?: "Recipe"
    return firstLine.trim()
        .replaceFirst(Regex("^[0-9]+[.)\\-]\\s*"), "")
        .replaceFirst(Regex("^[•\\-*]\\s*"), "")
        .take(50)
}

fun shareRecipe(context: android.content.Context, recipe: Recipe) {
    val recipeText = buildString {
        appendLine("🍳 ${recipe.name}")
        appendLine("From Mann Suvai - Fresh from your notebook!")
        appendLine()
        appendLine("Steps:")
        recipe.steps.forEachIndexed { index, step ->
            appendLine("${index + 1}. $step")
        }
        appendLine()
        appendLine("👨‍🍳 Shared via Mann Suvai App")
    }

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, recipeText)
        type = "text/plain"
    }

    context.startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
}
