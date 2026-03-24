package com.byteflipper.ui_components

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat

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
    val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(LocalContext.current))
        .build()

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
                    val safeUrl = contentUrl.toSafeWebViewUrl()
                    WebView(context).apply {
                        settings.javaScriptEnabled = false
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.allowFileAccessFromFileURLs = false
                        settings.allowUniversalAccessFromFileURLs = false

                        webViewClient = object : WebViewClientCompat() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest
                            ): WebResourceResponse? {
                                return assetLoader.shouldInterceptRequest(request.url)
                            }

                            override fun shouldInterceptRequest(
                                view: WebView?,
                                url: String?
                            ): WebResourceResponse? {
                                return url?.let { assetLoader.shouldInterceptRequest(Uri.parse(it)) }
                            }
                        }

                        loadUrl(safeUrl)
                    }
                },
                update = { webView ->
                    val safeUrl = contentUrl.toSafeWebViewUrl()
                    if (webView.url != safeUrl) {
                        webView.loadUrl(safeUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun String.toSafeWebViewUrl(): String {
    val assetPrefix = "file:///android_asset/"
    return if (startsWith(assetPrefix)) {
        val assetPath = removePrefix(assetPrefix)
        "https://appassets.androidplatform.net/assets/$assetPath"
    } else {
        this
    }
}
