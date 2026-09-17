package com.example.oatprediction.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.oatprediction.ui.StarterScreen

/**
 * Single-screen navigation entry point.
 * Add a NavHost when product screens and their routes are implemented.
 */
@Composable
fun OatNavigation(modifier: Modifier = Modifier) {
    StarterScreen(modifier = modifier)
}
