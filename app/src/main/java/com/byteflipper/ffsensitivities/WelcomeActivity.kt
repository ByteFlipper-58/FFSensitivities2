package com.byteflipper.ffsensitivities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.byteflipper.ffsensitivities.ui.screens.WelcomeScreen
import com.byteflipper.ffsensitivities.ui.theme.FFSensitivitiesTheme

class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isFirstLaunch = isFirstLaunch(context = this)

        if (!isFirstLaunch) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            setContent {
                FFSensitivitiesTheme(
                    darkTheme = true,
                    dynamicColor = false,
                    contrastTheme = false
                ){
                    WelcomeScreen(
                        onStartClick = {
                            setFirstLaunchCompleted(context = this)
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}