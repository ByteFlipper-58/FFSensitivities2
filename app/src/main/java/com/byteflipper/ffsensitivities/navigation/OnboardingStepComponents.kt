package com.byteflipper.ffsensitivities.navigation

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.navigation.Screen

/**
 * Компонент информационного шага для новой системы OnBoarding
 */
@Composable
fun OnboardingInfoStepContent(
    logoPainter: Painter,
    title: String,
    description: String,
    imageContentDescription: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = logoPainter,
            contentDescription = imageContentDescription,
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 32.dp)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Компонент шага разрешений для новой системы OnBoarding
 */
@Composable
fun OnboardingPermissionsStepContent(
    title: String,
    description: String,
    grantButtonText: String,
    grantedButtonText: String,
    isPermissionGranted: Boolean,
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onGrantPermissionClick,
            enabled = !isPermissionGranted,
            colors = if (isPermissionGranted) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
            } else {
                ButtonDefaults.buttonColors()
            }
        ) {
            Text(if (isPermissionGranted) grantedButtonText else grantButtonText)
        }
    }
}

/**
 * Компонент шага соглашения для новой системы OnBoarding
 */
@Composable
fun WelcomeAgreementStepContent(
    navController: NavController,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Соглашение
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Spacer(modifier = Modifier.width(4.dp))
            AcceptanceText(
                onShowPolicyClick = { documentType ->
                    navController.navigate(Screen.Policy(documentType).route)
                }
            )
        }
    }
}

/**
 * Кликабельный текст для принятия политики и условий
 */
@Composable
private fun AcceptanceText(
    onShowPolicyClick: (documentType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val fullText = stringResource(id = R.string.accept_terms_and_policy)
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
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "PRIVACY_POLICY", start = offset, end = offset)
                .firstOrNull()?.let { onShowPolicyClick("privacy_policy") }

            annotatedString.getStringAnnotations(tag = "TERMS_OF_USE", start = offset, end = offset)
                .firstOrNull()?.let { onShowPolicyClick("terms") }
        }
    )
} 