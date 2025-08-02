package com.byteflipper.ffsensitivities.presentation.sensitivities.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.components.SliderView

@Composable
fun SensitivitiesCard(
    deviceModel: DeviceModel
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = ShapeDefaults.ExtraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(21.dp)
        ) {
            SliderView(
                label = R.string.review,
                initialValue = deviceModel.sensitivities?.review?.toFloat() ?: 0f,
                onValueChange = {},
                enabled = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp
            )

            SliderView(
                label = R.string.collimator,
                initialValue = deviceModel.sensitivities?.collimator?.toFloat() ?: 0f,
                onValueChange = {},
                enabled = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp
            )

            SliderView(
                label = R.string.x2_scope,
                initialValue = deviceModel.sensitivities?.x2_scope?.toFloat() ?: 0f,
                onValueChange = {},
                enabled = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp
            )

            SliderView(
                label = R.string.x4_scope,
                initialValue = deviceModel.sensitivities?.x4_scope?.toFloat() ?: 0f,
                onValueChange = {},
                enabled = false
            )

            HorizontalDivider(
                modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 16.dp),
                thickness = 1.dp
            )

            Row {
                if (deviceModel.dpi == null || deviceModel.dpi == 0) {
                    Text(
                        text = stringResource(id = R.string.fire_button) + ": " + deviceModel.fire_button,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.dpi) + ": " + deviceModel.dpi + " | " + stringResource(
                            id = R.string.fire_button
                        ) + ": " + deviceModel.fire_button,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.it_works),
                    modifier = Modifier.weight(1f)
                )

                FilledTonalIconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.thumb_up_24px),
                        contentDescription = stringResource(R.string.favorite_icon_desc)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalIconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.thumb_down_24px),
                        contentDescription = stringResource(R.string.favorite_icon_desc)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                thickness = 1.dp
            )

            val clipboardManager = LocalClipboardManager.current

            val settingsText = buildString {
                append("${stringResource(id = R.string.dpi)}: ${deviceModel.dpi}\n")
                append("${stringResource(id = R.string.review)}: ${deviceModel.sensitivities?.review}\n")
                append("${stringResource(id = R.string.collimator)}: ${deviceModel.sensitivities?.collimator}\n")
                append("${stringResource(id = R.string.x2_scope)}: ${deviceModel.sensitivities?.x2_scope}\n")
                append("${stringResource(id = R.string.x4_scope)}: ${deviceModel.sensitivities?.x4_scope}\n")
                append("${stringResource(id = R.string.sniper_scope)}: ${deviceModel.sensitivities?.sniper_scope}\n")
                append("${stringResource(id = R.string.free_review)}: ${deviceModel.sensitivities?.free_review}\n")
                append("${stringResource(id = R.string.source)} ${deviceModel.settings_source_url}")
            }

            FilledTonalButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(settingsText))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.copy),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
} 