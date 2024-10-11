package com.byteflipper.ffsensitivities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.byteflipper.ffsensitivities.navigation.BottomNavigationBar
import com.byteflipper.ffsensitivities.navigation.NavigationHost
import com.byteflipper.ffsensitivities.ui.theme.FFSensitivitiesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityContent()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent() {
    val context = LocalContext.current
    val themePreference = getThemePreference(context)
    val dynamicColorPreference = true

    var currentTheme by remember { mutableStateOf(themePreference) }
    var currentDynamicColor by remember { mutableStateOf(dynamicColorPreference) }

    LaunchedEffect(themePreference, dynamicColorPreference) {
        currentTheme = themePreference
        currentDynamicColor = dynamicColorPreference
    }

    FFSensitivitiesTheme(
        themePreference = themePreference,
        dynamicColor = dynamicColorPreference) {

        val navController = rememberNavController()
        var toolbarTitle by remember { mutableStateOf("Главный экран") }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),

            topBar = {
                TopAppBar(
                    title = { Text(toolbarTitle) },
                    navigationIcon = {
                        if (currentRoute == "settings") {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад"
                                )
                            }
                        }
                    },
                    actions = {
                        if (currentRoute != "settings") {
                            IconButton(onClick = {
                                navController.navigate("settings")
                            }) {
                                Icon(Icons.Default.Settings, contentDescription = "Настройки")
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
                if (currentRoute != "settings") {
                    BottomNavigationBar(
                        navController = navController,
                    )
                }
            }
        ) { innerPadding ->
            NavigationHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onTitleChange = { newTitle -> toolbarTitle = newTitle }
            )
        }
    }
}

@PreviewDynamicColors
@Composable
@Preview(showBackground = true)
fun MainActivityPreview() {
    MainActivityContent()
}