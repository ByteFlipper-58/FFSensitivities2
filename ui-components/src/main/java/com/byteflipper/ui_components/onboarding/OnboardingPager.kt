package com.byteflipper.ui_components.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import kotlinx.coroutines.launch

/**
 * A composable that displays onboarding screens using a HorizontalPager.
 *
 * @param pages A list of OnboardingPage objects representing the screens to display.
 * @param navController The NavController for navigating within onboarding pages if needed.
 * @param isFinishEnabled A boolean indicating if the finish action is currently allowed (e.g., policy accepted).
 * @param onFinish Callback invoked when the user clicks the finish button on the last page.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPager(
    pages: List<OnboardingPage>,
    navController: NavController,
    isFinishEnabled: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val currentScreenIndex = pagerState.currentPage
    val totalScreens = pages.size

    Scaffold(
        modifier = modifier,
        bottomBar = {
            OnboardingBottomBar(
                totalScreens = totalScreens,
                currentScreenIndex = currentScreenIndex,
                onBackClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentScreenIndex - 1)
                    }
                },
                onNextClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentScreenIndex + 1)
                    }
                },
                onFinishClick = onFinish,
                backEnabled = currentScreenIndex > 0,
                // Enable finish only on the last page AND if the condition is met
                finishEnabled = currentScreenIndex == totalScreens - 1 && isFinishEnabled,
                // Enable next only if not on the last page
                nextEnabled = currentScreenIndex < totalScreens - 1
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            // Get the content composable for the current page
            val pageContent = pages[pageIndex].content
            // Provide NavController and padding to the page content
            // Box ensures the content fills the available space within the pager item
            Box(modifier = Modifier.fillMaxSize()) {
                 pageContent(navController, innerPadding) // Pass innerPadding if needed by the screen, though Scaffold handles the main padding
                // If individual screens need specific padding *within* the pager item,
                // they should apply it themselves or receive it differently.
                // Passing innerPadding here might not be what's intended if screens
                // expect to draw under the system bars handled by the outer Scaffold.
                // Let's pass PaddingValues() for now, assuming screens manage their own internal padding.
                // pageContent(navController, PaddingValues())
            }
        }
    }
}
