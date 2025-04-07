package com.byteflipper.ui_components

import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Используем AutoMirrored
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A reusable screen to display web content, typically for policies or terms.
 *
 * @param title The title to display in the TopAppBar.
 * @param contentUrl The URL (e.g., "file:///android_asset/...") of the content to load in the WebView.
 * @param onBackClick Lambda function to be invoked when the back navigation icon is clicked.
 * @param backContentDescription Content description for the back navigation icon.
 * @param modifier Modifier for the Scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolicyScreen(
    title: String,
    contentUrl: String,
    onBackClick: () -> Unit,
    backContentDescription: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backContentDescription
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
                        // Basic settings, consider adding more security settings if loading external URLs
                        settings.javaScriptEnabled = true // Be cautious with JS from untrusted sources
                        // Consider disabling file access and content access for external URLs:
                        // settings.allowFileAccess = false
                        // settings.allowContentAccess = false
                        loadUrl(contentUrl)
                    }
                },
                update = { webView ->
                    // Can be used to reload if the url changes, though not typical for this use case
                    // webView.loadUrl(contentUrl)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
