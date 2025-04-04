package com.byteflipper.ffsensitivities.presentation.ui.screens.onboarding // Новый пакет для специфичного контента

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R // Импорт ресурсов из app

/**
 * Content for the agreement page within the onboarding flow, specific to this app.
 */
@Composable
internal fun WelcomeAgreementContent( // internal, так как используется только внутри app
    navController: NavController,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues) // Apply padding from parent Pager/Scaffold
            .padding(horizontal = 16.dp), // Keep inner horizontal padding
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Center the main content
        Column(
            modifier = Modifier.weight(1f), // Takes available space
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Center vertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo), // Use app resources
                contentDescription = stringResource(R.string.app_icon), // Use app resources
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.welcome_screen_title), // Use app resources
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.welcome_screen_message), // Use app resources
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp)) // Space before checkbox

            // --- Agreement Row ---
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
                AcceptanceText( // Use local composable
                    onShowPolicyClick = { documentType ->
                        navController.navigate("policy/$documentType") // Navigation remains in app
                    }
                )
            }
        }
    }
}

/**
 * Clickable text for accepting privacy policy and terms, specific to this app.
 */
@Composable
private fun AcceptanceText(
    onShowPolicyClick: (documentType: String) -> Unit, // Lambda for link clicks
    modifier: Modifier = Modifier
) {
    val fullText = stringResource(id = R.string.privacy_acceptance_checkbox) // Use app resources
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
                // Improved logic for different document types
                val tag = when (span.url) {
                    "privacy" -> "PRIVACY_POLICY"
                    "terms" -> "TERMS_OF_USE"
                    else -> "UNKNOWN" // Handle potential unknown URLs
                }
                addStringAnnotation(
                    tag = tag,
                    annotation = span.url, // Store the original URL if needed
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
            annotatedString.getStringAnnotations(tag = "PRIVACY_POLICY", start = offset, end = offset)
                .firstOrNull()?.let { onShowPolicyClick("privacy_policy") }

            annotatedString.getStringAnnotations(tag = "TERMS_OF_USE", start = offset, end = offset)
                .firstOrNull()?.let { onShowPolicyClick("terms") }
        }
    )
}
