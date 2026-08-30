package com.mannsuvai.app

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

// --- Indian Theme Palette ---
val Saffron = Color(0xFFFF9933)
val Terracotta = Color(0xFF8B4513)
val WarmPaper = Color(0xFFFFF8DC)
val DeepGreen = Color(0xFF2E7D32)
val DeepRed = Color(0xFFC62828)
val CharcoalText = Color(0xFF2C2C2C)

data class EnhancedRecipe(
    val serialNumber: String,
    val title: String,
    val isVeg: Boolean,
    val rawText: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Saffron,
                    background = WarmPaper,
                    surface = WarmPaper
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WarmPaper
                ) {
                    MannSuvaiMainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MannSuvaiMainScreen() {
    var selectedCategory by remember { mutableStateOf<Boolean?>(null) } // true: Veg, false: Non-Veg, null: Selection Screen
    val recipes = remember { mutableStateListOf<EnhancedRecipe>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "மண் சுவை • Mann Suvai",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Terracotta
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = WarmPaper)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (selectedCategory == null) {
                CategorySelectionScreen(onSelectCategory = { isVeg -> selectedCategory = isVeg })
            } else {
                RecipeBookView(
                    isVeg = selectedCategory!!,
                    recipes = recipes.filter { it.isVeg == selectedCategory },
                    onBack = { selectedCategory = null },
                    onAddSampleScan = {
                        val newCount = recipes.size + 1
                        val serial = String.format("#%03d", newCount)
                        recipes.add(
                            EnhancedRecipe(
                                serialNumber = serial,
                                title = if (selectedCategory!!) "Handwritten Sambhar Notes" else "Handwritten Chicken Curry Notes",
                                isVeg = selectedCategory!!,
                                rawText = "Ingredients:\n- Mustard seeds 1 tsp\n- Curry leaves\n- Hand-ground Spices\n\nSteps:\n1. Temper spices in sesame oil.\n2. Simmer until aromatic."
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CategorySelectionScreen(onSelectCategory: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Recipe Book",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Terracotta,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Vegetarian Option
        Button(
            onClick = { onSelectCategory(true) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🟢 VEGETARIAN COOKBOOK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Non-Vegetarian Option
        Button(
            onClick = { onSelectCategory(false) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("🔴 NON-VEGETARIAN COOKBOOK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecipeBookView(
    isVeg: Boolean,
    recipes: List<EnhancedRecipe>,
    onBack: () -> Unit,
    onAddSampleScan: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { recipes.size + 1 })

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) {
                Text("← Categories", color = Terracotta)
            }

            Button(
                onClick = onAddSampleScan,
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Text("📷 Scan Handwritten")
            }
        }

        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recipes scanned yet.\nTap 'Scan Handwritten' to add your first page!",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = Terracotta,
                    fontFamily = FontFamily.Serif
                )
            }
        } else {
            // Kindle Page Turn Animation Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val recipe = recipes.getOrNull(page)
                if (recipe != null) {
                    KindleRecipePage(
                        recipe = recipe,
                        modifier = Modifier.graphicsLayer {
                            val pageOffset = (
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                            ).absoluteValue

                            // Kindle Page Curl / Scale effect
                            alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            scaleY = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KindleRecipePage(recipe: EnhancedRecipe, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(WarmPaper, shape = RoundedCornerShape(8.dp))
            .border(2.dp, Terracotta.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Serial Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recipe.serialNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (recipe.isVeg) "Veg" else "Non-Veg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (recipe.isVeg) DeepGreen else DeepRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = recipe.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Terracotta,
                fontFamily = FontFamily.Serif
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Terracotta.copy(alpha = 0.3f))

            // Text extracted from handwritten recipe
            Text(
                text = recipe.rawText,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = CharcoalText,
                fontFamily = FontFamily.Serif
            )
        }

        // Page footer
        Text(
            text = "Mann Suvai Kindle Reader",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
