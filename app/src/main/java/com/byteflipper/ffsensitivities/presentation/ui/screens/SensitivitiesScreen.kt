package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.app.Activity
import android.util.Log
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
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.AppViewModel
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.ads.InterstitialAdManager
import com.byteflipper.ffsensitivities.domain.model.DeviceModel
import com.byteflipper.ffsensitivities.presentation.ui.components.SliderView
import com.google.gson.Gson

@Composable
fun SensitivitiesScreen(
    navController: NavController,
    device: String?,
    appViewModel: AppViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 0.dp, 16.dp, 0.dp)
    ) {

        val context = LocalContext.current
        val interstitialAdManager = remember { InterstitialAdManager(context) }
        val activity = context as Activity

        DisposableEffect(Unit) {
            onDispose {
                interstitialAdManager.destroy()
            }
        }

        val visitCountState by appViewModel.visitCount.collectAsState()

        LaunchedEffect(Unit) {
            val currentVisitCount = visitCountState
            val newVisitCount = currentVisitCount + 1
            appViewModel.setVisitCount(newVisitCount) // Инкрементируем через ViewModel

            if (newVisitCount % 3 == 0) {
                interstitialAdManager.loadAd(
                    adUnitId = "R-M-13549181-3", // TODO: Replace with your actual Ad Unit ID
                    onLoaded = {
                        interstitialAdManager.show(activity) // Pass activity context here
                    },
                    onError = { error ->
                        Log.e("SensitivitiesScreen", "Interstitial Ad failed to load: ${error.description}")
                    },
                    onShown = {
                        appViewModel.setVisitCount(0) // Сбрасываем через ViewModel
                    },
                    onDismissed = {
                        // Можно добавить обработку закрытия рекламы
                    }
                )
            }
        }

        navController.currentBackStackEntry?.arguments?.getString("name") ?: ""
        val deviceModel = Gson().fromJson(device, DeviceModel::class.java)

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
                    initialValue = deviceModel.sensitivities?.review?.toFloat() ?: 0f,
                    onValueChange = { slider1Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp
                )

                SliderView(
                    label = R.string.collimator,
                    initialValue = deviceModel.sensitivities?.collimator?.toFloat() ?: 0f,
                    onValueChange = { slider2Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp
                )

                SliderView(
                    label = R.string.x2_scope,
                    initialValue = deviceModel.sensitivities?.x2_scope?.toFloat() ?: 0f,
                    onValueChange = { slider3Value = it },
                    enabled = false
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp
                )

                SliderView(
                    label = R.string.x4_scope,
                    initialValue = deviceModel.sensitivities?.x4_scope?.toFloat() ?: 0f,
                    onValueChange = { slider3Value = it },
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
}
