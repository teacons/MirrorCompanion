package ru.fbear.mirror_companion.settings

import Spinnable
import Spinner
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import ru.fbear.mirror_companion.CompanionViewModel

@Composable
fun GuestScreenSettings(viewModel: CompanionViewModel = viewModel()) {

    val settings by viewModel.settings.observeAsState()

    val fonts by viewModel.fonts.observeAsState(emptyList())

    var guestHelloTextError by rememberSaveable { mutableStateOf(false) }
    var guestShootTimerError by rememberSaveable { mutableStateOf(false) }
    var guestShootTextError by rememberSaveable { mutableStateOf(false) }
    var guestWaitTextError by rememberSaveable { mutableStateOf(false) }
    var guestTextFontSizeError by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = settings?.guestHelloText ?: "",
            onValueChange = {
                if (it.isNotEmpty()) {
                    viewModel.settings.value = viewModel.settings.value?.copy(guestHelloText = it)
                    guestHelloTextError = false
                } else guestHelloTextError = true
            },
            maxLines = 2,
            isError = guestHelloTextError,
            label = { Text(text = "Текст приветствия") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        OutlinedTextField(
            value = settings?.guestShootText ?: "",
            onValueChange = {
                if (it.isNotEmpty()) {
                    viewModel.settings.value = viewModel.settings.value?.copy(guestShootText = it)
                    guestShootTextError = false
                } else guestShootTextError = true
            },
            maxLines = 2,
            isError = guestShootTextError,
            label = { Text(text = "Текст съёмки") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        OutlinedTextField(
            value = settings?.guestWaitText ?: "",
            onValueChange = {
                if (it.isNotEmpty()) {
                    viewModel.settings.value = viewModel.settings.value?.copy(guestWaitText = it)
                    guestWaitTextError = false
                } else guestWaitTextError = true
            },
            maxLines = 2,
            isError = guestWaitTextError,
            label = { Text(text = "Текст ожидания фотографии") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        OutlinedTextField(
            value = settings?.guestShootTimer?.toString() ?: "",
            onValueChange = {
                if (it.toIntOrNull() != null) {
                    viewModel.settings.value = viewModel.settings.value?.copy(guestShootTimer = it.toInt())
                    guestShootTimerError = false
                } else guestShootTimerError = true
            },
            maxLines = 1,
            isError = guestShootTimerError,
            label = { Text(text = "Значение таймера") },
            modifier = Modifier.fillMaxWidth()
        )
        Spinner(
            data = fonts.map {
                object : Spinnable {
                    override fun toString() = it
                }
            },
            value = settings?.guestTextFontFamily ?: "",
            onSelectedChanges = {
                viewModel.settings.value = viewModel.settings.value?.copy(guestTextFontFamily = it.toString())
            },
            label = { Text("Шрифт") },
        ) {
            Text(
                text = it.toString()
            )
        }
        OutlinedTextField(
            value = settings?.guestTextFontSize?.toString() ?: "",
            onValueChange = {
                if (it.toIntOrNull() != null) {
                    viewModel.settings.value = viewModel.settings.value?.copy(guestTextFontSize = it.toInt())
                    guestTextFontSizeError = false
                } else guestTextFontSizeError = true
            },
            maxLines = 1,
            isError = guestTextFontSizeError,
            label = { Text(text = "Размер шрифта") },
            modifier = Modifier.fillMaxWidth()
        )
        HarmonyColorPicker(
            harmonyMode = ColorHarmonyMode.SHADES,
            modifier = Modifier.size(350.dp),
            onColorChanged = { hsvColor ->
                viewModel.settings.value = viewModel.settings.value?.copy(guestTextFontColor = hsvColor.toColor().value)
            },
            color = Color(settings?.guestTextFontColor ?: Color.Red.value)
        )
    }
}