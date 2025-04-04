package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R

@OptIn(ExperimentalMaterial3Api::class) // Add if not present
@Composable
fun WelcomeScreen(
    navController: NavController,
    // onContinueClick is now handled by OnboardingBottomBar in Activity
    // onContinueClick: () -> Unit,
    isChecked: Boolean, // Add state parameter
    onCheckedChange: (Boolean) -> Unit, // Add lambda parameter
    paddingValues: PaddingValues, // Add padding parameter
    modifier: Modifier = Modifier
) {
    // Remove internal state management
    // var isChecked by remember { mutableStateOf(false) }

    // Remove Scaffold wrapper
    // Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = modifier // Use passed modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from parent Scaffold
                .padding(horizontal = 16.dp), // Keep horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
             // Keep the main content centered vertically
            Column(
                modifier = Modifier.weight(1f), // Takes available space above the bottom bar
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Center the content
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = stringResource(R.string.app_icon),
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(32.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.welcome_screen_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.welcome_screen_message),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp)) // Add some space before the checkbox

                // --- Acceptance Row --- (Keep this part for policy agreement)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked, // Use passed state
                        onCheckedChange = onCheckedChange // Use passed lambda
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AcceptanceText(
                        onShowPolicyClick = { documentType ->
                            navController.navigate("policy/$documentType")
                        }
                    )
                }
            }
            // Remove OnboardingBottomBar from here
        }
    // } // End of removed Scaffold
}

// Keep AcceptanceText and PolicyScreen functions as they are part of this file's logic
@Composable
private fun AcceptanceText(
    onShowPolicyClick: (documentType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fullText = stringResource(id = R.string.privacy_acceptance_checkbox)
    val parsedText = HtmlCompat.fromHtml(fullText, HtmlCompat.FROM_HTML_MODE_LEGACY)

    val annotatedString = buildAnnotatedString {
        append(parsedText)
        parsedText.getSpans(0, parsedText.length, android.text.style.URLSpan::class.java)
            .forEach { span ->
                val start = parsedText.getSpanStart(span)
                val end = parsedText.getSpanEnd(span)
                addStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )
                // Улучшенная логика для разных типов документов
                val tag = when (span.url) {
                    "privacy" -> "PRIVACY_POLICY"
                    "terms" -> "TERMS_OF_USE"
                    else -> "UNKNOWN"
                }
                addStringAnnotation(
                    tag = tag,
                    annotation = span.url,
                    start = start,
                    end = end
                )
            }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    when (annotation.tag) {
                        "PRIVACY_POLICY" -> onShowPolicyClick("privacy_policy")
                        "TERMS_OF_USE" -> onShowPolicyClick("terms")
                    }
                }
        }
    )
}

// Новый экран для отображения документов вместо диалога
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    documentType: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val assetFileName = when (documentType) {
        "privacy_policy" -> "privacy_policy.html"
        "terms" -> "terms.html"
        else -> "privacy_policy.html" // Дефолтное значение
    }

    val title = when (documentType) {
        "privacy_policy" -> stringResource(R.string.privacy_dialog_title)
        "terms" -> stringResource(R.string.terms_of_service)
        else -> stringResource(R.string.privacy_dialog_title)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        loadUrl("file:///android_asset/$assetFileName")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// Навигационная конфигурация - добавьте в ваш NavHost
/*
NavHost(navController = navController, startDestination = "welcome") {
    composable("welcome") {
        WelcomeScreen(
            navController = navController,
            onContinueClick = { /* Ваш код */ }
        )
    }
    composable(
        "policy/{documentType}",
        arguments = listOf(navArgument("documentType") { type = NavType.StringType })
    ) { backStackEntry ->
        val documentType = backStackEntry.arguments?.getString("documentType") ?: "privacy_policy"
        PolicyScreen(
            documentType = documentType,
            navController = navController
        )
    }
}
*/
