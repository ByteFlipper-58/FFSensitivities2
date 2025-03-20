package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.content.Context
import android.text.Html
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import java.io.IOException

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogContent by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp, 32.dp, 8.dp, 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(120.dp))

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier
                .size(164.dp)
                .clip(RoundedCornerShape(50))
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(id = R.string.welcome_screen_title),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )

        Text(
            text = stringResource(id = R.string.welcome_screen_message),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onStartClick,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.continue_button),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val privacyPolicyMessage = stringResource(id = R.string.privacy_policy_message)
        val termsOfService = stringResource(id = R.string.terms_of_service)
        val privacyPolicy = stringResource(id = R.string.privacy_policy)

        val annotatedString = buildAnnotatedString {
            append(privacyPolicyMessage)

            val termsStart = privacyPolicyMessage.indexOf(termsOfService)
            val privacyPolicyStart = privacyPolicyMessage.indexOf(privacyPolicy)

            if (termsStart >= 0) {
                addStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                    start = termsStart,
                    end = termsStart + termsOfService.length
                )
                addStringAnnotation(
                    tag = "TERMS",
                    annotation = "terms",
                    start = termsStart,
                    end = termsStart + termsOfService.length
                )
            }

            if (privacyPolicyStart >= 0) {
                addStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                    start = privacyPolicyStart,
                    end = privacyPolicyStart + privacyPolicy.length
                )
                addStringAnnotation(
                    tag = "POLICY",
                    annotation = "policy",
                    start = privacyPolicyStart,
                    end = privacyPolicyStart + privacyPolicy.length
                )
            }
        }

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                    .firstOrNull()?.let {
                        dialogTitle = context.getString(R.string.terms_of_service)
                        dialogContent = readFileFromAssets(context, "terms.html")
                        showDialog = true
                    }

                annotatedString.getStringAnnotations(tag = "POLICY", start = offset, end = offset)
                    .firstOrNull()?.let {
                        dialogTitle = context.getString(R.string.privacy_policy)
                        dialogContent = readFileFromAssets(context, "privacy.html")
                        showDialog = true
                    }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle) },
            text = {
                val formattedContent = Html.fromHtml(dialogContent, Html.FROM_HTML_MODE_COMPACT).toString()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = formattedContent
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = "OK")
                }
            }
        )
    }
}

fun readFileFromAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}