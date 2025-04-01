package com.byteflipper.ffsensitivities.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.byteflipper.ffsensitivities.data.local.isFirstLaunch
import com.byteflipper.ffsensitivities.data.local.setFirstLaunchCompleted
import com.byteflipper.ffsensitivities.presentation.ui.screens.WelcomeScreen

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
