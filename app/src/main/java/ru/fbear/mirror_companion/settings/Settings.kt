package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Settings(
    var cameraName: String,

    var printerName: String,

    var printerMediaSizeName: String,

    var photoserverEnabled: Boolean,
    var photoserverAddress: String,

    var layout: String,

    var guestHelloText: String,
    var guestShootText: String,
    var guestWaitText: String,
    var guestShootTimer: Int,
    var guestBackgroundFilepath: String,
    var guestTextFontFamily: String,
    var guestTextFontSize: Int,
    var guestTextFontColor: ULong
)

@Composable
fun Settings() {

    var selectedMenuItem by rememberSaveable { mutableStateOf(MenuItem.PhotoCamera) }

    Scaffold(
        drawerContent = {
            MenuItem.values().forEach {
                MenuItem(it, selectedMenuItem) {
                    selectedMenuItem = it
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

            }
        }
    }
}




