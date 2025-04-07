package com.byteflipper.ffsensitivities.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.BuildConfig
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.utils.ChromeCustomTabUtil
import com.byteflipper.ui_components.components.AnimatedActionItem
import com.byteflipper.ui_components.components.ExpandableSection
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val reviewManager = remember { ReviewManagerFactory.create(context) }

    fun launchReviewFlow() {
        val activity = context as? ComponentActivity ?: return
        coroutineScope.launch {
            try {
                val reviewInfo = reviewManager.requestReviewFlow().await() // Используем await() для получения ReviewInfo
                reviewManager.launchReviewFlow(activity, reviewInfo) // Передаем полученный ReviewInfo
            } catch (e: Exception) {
                // Обработка ошибки: открываем страницу приложения в Play Store
                println("In-App Review failed, opening Play Store: ${e.message}")
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (activityNotFoundException: Exception) {
                    // Если Play Store не найден, открываем веб-версию
                    println("Play Store not found, opening web URL: ${activityNotFoundException.message}")
                    try {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(webIntent)
                    } catch (webException: Exception) {
                        println("Failed to open web URL: ${webException.message}")
                        // Можно добавить дополнительную обработку, если и веб-ссылка не открылась
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_app)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppInfoHeader()

            ExpandableSection(
                title = stringResource(R.string.support_feedback_category_title),
                icon = painterResource(id = R.drawable.bug_report_24px),
                expandedContentDescription = stringResource(R.string.expandable_section_expand),
                collapsedContentDescription = stringResource(R.string.expandable_section_collapse)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedActionItem(
                        title = stringResource(R.string.bug_report_title),
                        subtitle = stringResource(R.string.bug_report_subtitle),
                        icon = painterResource(id = R.drawable.bug_report_24px),
                        onClick = { navController.navigate("bug_report") }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.rate_the_app_title),
                        subtitle = stringResource(R.string.rate_the_app_subtitle),
                        icon = painterResource(id = R.drawable.rate_review_24px),
                        onClick = { launchReviewFlow() }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.other_apps_title),
                        subtitle = stringResource(R.string.other_apps_subtitle),
                        icon = painterResource(id = R.drawable.apps_24px),
                        onClick = { /* TODO: Add other apps action */ },
                        showDivider = false // Hide divider for the last item
                    )
                }
            }

            ExpandableSection(
                title = stringResource(R.string.connect_with_us_category_title),
                icon = painterResource(id = R.drawable.web_24px),
                expandedContentDescription = stringResource(R.string.expandable_section_expand),
                collapsedContentDescription = stringResource(R.string.expandable_section_collapse)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedActionItem(
                        title = stringResource(R.string.website_title),
                        subtitle = stringResource(R.string.website_subtitle),
                        icon = painterResource(id = R.drawable.web_24px),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://byteflipper.web.app",
                                )
                        }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.vk_title),
                        subtitle = stringResource(R.string.vk_subtitle),
                        icon = painterResource(id = R.drawable.vk_24),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://vk.com/byteflipper",
                                )
                        }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.telegram_title),
                        subtitle = stringResource(R.string.telegram_subtitle),
                        icon = painterResource(id = R.drawable.telegram_24),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://t.me/byteflipper",
                                )
                        },
                        showDivider = false
                    )
                }
            }

            ExpandableSection(
                title = stringResource(R.string.development_category_title),
                icon = painterResource(id = R.drawable.code_24px),
                expandedContentDescription = stringResource(R.string.expandable_section_expand),
                collapsedContentDescription = stringResource(R.string.expandable_section_collapse)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedActionItem(
                        title = stringResource(R.string.github_title),
                        subtitle = stringResource(R.string.github_subtitle),
                        icon = painterResource(id = R.drawable.github_24),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://github.com/ByteFlipper-58",
                                )
                        }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.source_code_title),
                        subtitle = stringResource(R.string.source_code_subtitle),
                        icon = painterResource(id = R.drawable.code_24px),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://github.com/ByteFlipper-58/FFSensitivities2",
                                )
                        },
                        showDivider = false
                    )
                }
            }

            VersionInfoCard()

            Card(
                modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.made_with_love),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun AppInfoHeader() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 6.dp
        ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .shadow(8.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "ByteFlipper",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Add action */ },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.contact_us),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun VersionInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.update_24px),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.version),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
