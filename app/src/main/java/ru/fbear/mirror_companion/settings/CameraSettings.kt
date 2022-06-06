package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.fbear.mirror_companion.CompanionViewModel
import ru.fbear.mirror_companion.Spinnable
import ru.fbear.mirror_companion.Spinner


@Composable
fun CameraSettings(viewModel: CompanionViewModel = viewModel()) {
    val settings by viewModel.settings.observeAsState()

    val cameraList by viewModel.cameras.observeAsState(emptyList())

    val cameraConfigs by viewModel.cameraConfigs.observeAsState(emptyList())

    val isRefreshingCameras by viewModel.isRefreshingCameras.observeAsState(false)

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshingCameras),
        onRefresh = { viewModel.refreshCameras() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
        ) {
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
            cameraConfigs.forEach { config ->
                Spinner(
                    data = config.choices.map {
                        object : Spinnable {
                            override fun toString() = it
                        }
                    },
                    value = config.value,
                    onSelectedChanges = {
                        viewModel.updateCameraConfigValue(config.configName, it.toString())
                    },
                    label = { Text(text = config.configName) }
                ) {
                    Text(text = it.toString())
                }
            }
        }
    }
}