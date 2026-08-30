package com.mannsuvai.app

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * ShareManager
 * Handles sharing recipes to various social media platforms and applications
 */
object ShareManager {

    /**
     * Share recipe to all available platforms via chooser
     */
    fun shareRecipeDefault(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share Recipe")
        )
    }

    /**
     * Share directly to WhatsApp
     */
    fun shareToWhatsApp(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        val uri = Uri.parse(
            "https://wa.me/?text=${java.net.URLEncoder.encode(shareText, "UTF-8")}"
        )
        
        val intent = Intent(Intent.ACTION_VIEW, uri)
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // WhatsApp not installed, open browser
            openUrl(context, uri.toString())
        }
    }

    /**
     * Share to WhatsApp Business
     */
    fun shareToWhatsAppBusiness(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
            setPackage("com.whatsapp.w4b")  // WhatsApp Business package
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareRecipeDefault(context, recipe)
        }
    }

    /**
     * Share to Facebook
     */
    fun shareToFacebook(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        // Facebook doesn't support direct sharing of text via Intent
        // Open Facebook app or web with share dialog
        val facebookUrl = "https://www.facebook.com/sharer/sharer.php?u=mannsuvai.app"
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl))
        intent.setPackage("com.facebook.katana")  // Facebook app
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl(context, facebookUrl)
        }
    }

    /**
     * Share to Messenger
     */
    fun shareToMessenger(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
            setPackage("com.facebook.orca")  // Messenger package
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareRecipeDefault(context, recipe)
        }
    }

    /**
     * Share via Email
     */
    fun shareViaEmail(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        val subject = "🍳 ${recipe.name} - From Mann Suvai"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        try {
            context.startActivity(
                Intent.createChooser(intent, "Send Recipe via Email")
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Share via SMS
     */
    fun shareViaSMS(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        val smsUri = Uri.parse("smsto:")
        
        val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
            putExtra("sms_body", shareText)
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Share to Twitter/X
     */
    fun shareToTwitter(context: Context, recipe: Recipe) {
        val tweetText = "Just made ${recipe.name} using @MannSuvaiApp! 🍳👨‍🍳 #cooking #recipes"
        val twitterUrl = "https://twitter.com/intent/tweet?text=${java.net.URLEncoder.encode(tweetText, "UTF-8")}"
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl))
        intent.setPackage("com.twitter.android")
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrl(context, twitterUrl)
        }
    }

    /**
     * Share to Telegram
     */
    fun shareToTelegram(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
            setPackage("org.telegram.messenger")
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareRecipeDefault(context, recipe)
        }
    }

    /**
     * Export recipe as text file
     */
    fun exportRecipeAsText(context: Context, recipe: Recipe): Uri? {
        return try {
            val recipeText = buildRecipeShareText(recipe)
            val fileName = "${recipe.name.replace(" ", "_")}.txt"
            val file = File(context.cacheDir, fileName)
            
            file.writeText(recipeText)
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Export recipe as PDF (requires PDF library)
     */
    fun exportRecipeAsPDF(context: Context, recipe: Recipe): Uri? {
        return try {
            // This requires adding a PDF library like iText or pdfbox
            // Placeholder for PDF generation
            val fileName = "${recipe.name.replace(" ", "_")}.pdf"
            val file = File(context.cacheDir, fileName)
            
            // PDF generation would go here
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copy recipe to clipboard
     */
    fun copyToClipboard(context: Context, recipe: Recipe) {
        val shareText = buildRecipeShareText(recipe)
        
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) 
            as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Recipe", shareText)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Check if specific app is installed
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get list of available sharing apps
     */
    fun getAvailableSharingApps(context: Context): List<ShareOption> {
        return listOf(
            ShareOption("WhatsApp", "com.whatsapp") { shareToWhatsApp(context, it) },
            ShareOption("WhatsApp Business", "com.whatsapp.w4b") { shareToWhatsAppBusiness(context, it) },
            ShareOption("Messenger", "com.facebook.orca") { shareToMessenger(context, it) },
            ShareOption("Telegram", "org.telegram.messenger") { shareToTelegram(context, it) },
            ShareOption("Twitter", "com.twitter.android") { shareToTwitter(context, it) },
            ShareOption("Email", null) { shareViaEmail(context, it) },
            ShareOption("SMS", null) { shareViaSMS(context, it) }
        ).filter { option ->
            // Show all options, but some may need app installed
            option.packageName == null || isAppInstalled(context, option.packageName)
        }
    }

    /**
     * Build formatted recipe text for sharing
     */
    private fun buildRecipeShareText(recipe: Recipe): String {
        return buildString {
            appendLine("🍳 ${recipe.name}")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("📝 Steps:")
            appendLine()
            
            recipe.steps.forEachIndexed { index, step ->
                appendLine("${index + 1}. $step")
            }
            
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            appendLine("👨‍🍳 Created with Mann Suvai")
            appendLine("Transform your handwritten recipes into interactive guides")
            appendLine()
            appendLine("#MannSuvai #RecipeApp #Cooking #FoodBlogger")
        }
    }

    /**
     * Open URL in browser
     */
    private fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

/**
 * Data class for share options
 */
data class ShareOption(
    val name: String,
    val packageName: String?,
    val action: (Recipe) -> Unit
)

/**
 * Share menu implementation in Compose
 */
@Composable
fun ShareMenuBottomSheet(
    recipe: Recipe,
    onDismiss: () -> Unit,
    context: android.content.Context
) {
    val shareOptions = remember { ShareManager.getAvailableSharingApps(context) }
    
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Share Recipe",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFFD4A574)
            )

            shareOptions.forEach { option ->
                Button(
                    onClick = {
                        option.action(recipe)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2D2D2D)
                    )
                ) {
                    Text(
                        option.name,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                        color = Color(0xFFD4A574)
                    )
                }
            }

            // Copy to clipboard option
            Button(
                onClick = {
                    ShareManager.copyToClipboard(context, recipe)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2D2D2D)
                )
            ) {
                Text(
                    "📋 Copy to Clipboard",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                    color = Color(0xFFD4A574)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
