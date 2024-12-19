package com.byteflipper.ffsensitivities.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.InterstitialAdManager
import com.byteflipper.ffsensitivities.data.DeviceModel
import com.byteflipper.ffsensitivities.ui.components.SliderView
import com.google.gson.Gson

//@Preview(showBackground = true)
@Composable
fun SensitivitiesScreen(
    navController: NavController,
    device: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp, 16.dp, 0.dp)
    ) {

        navController.currentBackStackEntry?.arguments?.getString("name") ?: ""
        val deviceModel = Gson().fromJson(device, DeviceModel::class.java)

        val context = LocalContext.current as Activity
        val interstitialAdManager = remember { InterstitialAdManager(context) }

        DisposableEffect(Unit) {
            onDispose {
                interstitialAdManager.destroy()
            }
        }

        LaunchedEffect(Unit) {
            interstitialAdManager.loadAd(
                adUnitId = "R-M-11993742-3",
                onLoaded = {
                    interstitialAdManager.show()
                    Toast.makeText(context, "Ad loaded", Toast.LENGTH_SHORT).show() },
                onError = { Toast.makeText(context, "Ad failed to load", Toast.LENGTH_SHORT).show() },
                onShown = { Toast.makeText(context, "Ad shown", Toast.LENGTH_SHORT).show() },
                onDismissed = { Toast.makeText(context, "Ad dismissed", Toast.LENGTH_SHORT).show() }
            )
        }

        var slider1Value by remember { mutableFloatStateOf(50f) }
        var slider2Value by remember { mutableFloatStateOf(75f) }
        var slider3Value by remember { mutableFloatStateOf(25f) }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth(),
            shape = ShapeDefaults.ExtraLarge
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(21.dp)
            ) {
                SliderView(
                    label = R.string.review,
                    initialValue = deviceModel.sensitivities.review.toFloat(),
                    onValueChange = { slider1Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp
                )

                SliderView(
                    label = R.string.collimator,
                    initialValue = deviceModel.sensitivities.collimator.toFloat(),
                    onValueChange = { slider2Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp
                )

                SliderView(
                    label = R.string.x2_scope,
                    initialValue = deviceModel.sensitivities.x2_scope.toFloat(),
                    onValueChange = { slider3Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp
                )

                SliderView(
                    label = R.string.x4_scope,
                    initialValue = deviceModel.sensitivities.x4_scope.toFloat(),
                    onValueChange = { slider3Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 16.dp),
                    thickness = 1.dp
                )

                Row {
                    if (deviceModel.dpi == 0) {
                        Text(
                            text = stringResource(id = R.string.fire_button) + ": " + deviceModel.fire_button,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.dpi) + ": " + deviceModel.dpi + " | " + stringResource(id = R.string.fire_button) + ": " + deviceModel.fire_button,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.it_works),
                        modifier = Modifier.weight(1f)
                    )

                    FilledTonalIconButton(
                        onClick = { /* Действие для первой иконки */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = { /* Действие для второй иконки */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite"
                        )
                    }
                }


                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    thickness = 1.dp
                )

                val clipboardManager = LocalClipboardManager.current

                val settingsText = buildString {
                    append("${stringResource(id = R.string.dpi)}: ${deviceModel.dpi.toInt()}\n")
                    append("${stringResource(id = R.string.review)}: ${deviceModel.sensitivities.review.toInt()}\n")
                    append("${stringResource(id = R.string.collimator)}: ${deviceModel.sensitivities.collimator.toInt()}\n")
                    append("${stringResource(id = R.string.x2_scope)}: ${deviceModel.sensitivities.x2_scope.toInt()}\n")
                    append("${stringResource(id = R.string.x4_scope)}: ${deviceModel.sensitivities.x4_scope.toInt()}\n")
                    append("${stringResource(id = R.string.sniper_scope)}: ${deviceModel.sensitivities.sniper_scope.toInt()}\n")
                    append("${stringResource(id = R.string.free_review)}: ${deviceModel.sensitivities.free_review.toInt()}\n")
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
}