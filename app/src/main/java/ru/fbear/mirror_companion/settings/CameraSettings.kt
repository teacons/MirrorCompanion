package ru.fbear.mirror_companion.settings

import Spinnable
import Spinner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fbear.mirror_companion.CompanionViewModel


@Composable
fun CameraSettings(viewModel: CompanionViewModel = viewModel()) {
    val settings by viewModel.settings.observeAsState()

    val cameraList by viewModel.cameras.observeAsState(emptyList())

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spinner(
            data = cameraList.map {
                object : Spinnable {
                    override fun toString() = it
                }
            },
            value = settings?.cameraName ?: "",
            onSelectedChanges = {
                viewModel.settings.value = viewModel.settings.value?.copy(cameraName = it.toString())
            },
            label = { Text(text = "Камера") }
        ) {
            Text(text = it.toString())
        }
    }
}