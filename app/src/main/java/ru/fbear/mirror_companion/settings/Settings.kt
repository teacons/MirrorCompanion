package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fbear.mirror_companion.CompanionViewModel

data class CameraConfigEntry(
    val configName: String,
    val value: String,
    val choices: List<String>
)

data class Settings(
    var cameraName: String?,

    var printerName: String?,

    var printerMediaSizeName: String?,

    var photoserverEnabled: Boolean?,
    var photoserverAddress: String?,

    var layout: String?,

    var guestHelloText: String?,
    var guestShootText: String?,
    var guestWaitText: String?,
    var guestShootTimer: Int?,
    var guestBackgroundFilepath: String?,
    var guestTextFontFamily: String?,
    var guestTextFontSize: Int?,
    var guestTextFontColor: ULong?
)

@Composable
fun Settings(viewModel: CompanionViewModel = viewModel(), onShutdown: () -> Unit) {

    var selectedMenuItem by rememberSaveable { mutableStateOf(MenuItem.PhotoCamera) }

    val incomingChanges by viewModel.incomingChanges.observeAsState(false)

    val isShutdown by viewModel.mirrorIsShutdown.observeAsState(false)

    if (isShutdown) onShutdown()

    Scaffold(
        drawerContent = {
            MenuItem.values().forEach {
                MenuItem(it, selectedMenuItem) {
                    selectedMenuItem = it
                }
            }
        },
        floatingActionButton = {
            if (incomingChanges) {
                FloatingActionButton(
                    onClick = {
                        viewModel.sendNewSettings()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier.padding(15.dp).fillMaxSize()
        ) {
            when (selectedMenuItem) {
                MenuItem.PhotoCamera -> CameraSettings()
                MenuItem.Printer -> PrinterSettings()
                MenuItem.PhotoServer -> PhotoserverSettings()
                MenuItem.GuestScreen -> GuestScreenSettings()
                MenuItem.Control -> Control()
            }
        }
    }
}




