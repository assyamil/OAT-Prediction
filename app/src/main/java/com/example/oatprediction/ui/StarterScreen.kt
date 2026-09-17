package com.example.oatprediction.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.oatprediction.R
import com.example.oatprediction.ui.theme.OATPredictionTheme

/** Existing starter screen; the product Home screen belongs to a later phase. */
@Composable
fun StarterScreen(modifier: Modifier = Modifier) {
    Text(text = stringResource(R.string.starter_greeting), modifier = modifier)
}

@Preview(showBackground = true)
@Composable
private fun StarterScreenPreview() {
    OATPredictionTheme {
        StarterScreen()
    }
}
