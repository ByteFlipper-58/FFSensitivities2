package com.byteflipper.ffsensitivities.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp, 16.dp, 0.dp)

    ) {
        item {
            ElevatedCard(shape = ShapeDefaults.Large) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(128.dp)
                    )
                    Text(text = stringResource(R.string.app_name))
                    Text(text = "ByteFlipper")
                    ListItem(
                        title = stringResource(R.string.bug_report_title),
                        subtitle = stringResource(R.string.bug_report_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.rate_the_app_title),
                        subtitle = stringResource(R.string.rate_the_app_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.other_apps_title),
                        subtitle = stringResource(R.string.other_apps_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ElevatedCard(shape = ShapeDefaults.Large) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ListItem(
                        title = stringResource(R.string.website_title),
                        subtitle = stringResource(R.string.website_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.vk_title),
                        subtitle = stringResource(R.string.vk_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.telegram_title),
                        subtitle = stringResource(R.string.telegram_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.github_title),
                        subtitle = stringResource(R.string.github_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.source_code_title),
                        subtitle = stringResource(R.string.source_code_subtitle),
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                    HorizontalDivider(Modifier.padding(16.dp, 0.dp, 16.dp, 0.dp))
                    ListItem(
                        title = stringResource(R.string.version),
                        subtitle = "v2.0.0",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ListItem(
    title: String = "Title",
    subtitle: String = "Subtitle",
    icon: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(end = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun AboutScreenPreview() {
    AboutScreen()
}