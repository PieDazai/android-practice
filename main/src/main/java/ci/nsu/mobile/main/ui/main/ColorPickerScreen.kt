package ci.nsu.mobile.main.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ColorPickerScreen(
    viewModel: ColorPickerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.padding(16.dp)) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(uiState.color)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = uiState.hexCode)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Red: ${uiState.red}")
        Slider(
            value = uiState.red.toFloat(),
            onValueChange = { viewModel.onRedChanged(it) },
            valueRange = 0f..255f
        )

        Text("Green: ${uiState.green}")
        Slider(
            value = uiState.green.toFloat(),
            onValueChange = { viewModel.onGreenChanged(it) },
            valueRange = 0f..255f
        )

        Text("Blue: ${uiState.blue}")
        Slider(
            value = uiState.blue.toFloat(),
            onValueChange = { viewModel.onBlueChanged(it) },
            valueRange = 0f..255f
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.generateRandomColor() }) {
            Text("Случайный цвет")
        }
    }
}