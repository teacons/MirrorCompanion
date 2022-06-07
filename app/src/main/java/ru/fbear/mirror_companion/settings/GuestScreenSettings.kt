package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.godaddy.android.colorpicker.harmony.HarmonyColorPicker
import ru.fbear.mirror_companion.CompanionViewModel
import ru.fbear.mirror_companion.Spinnable
import ru.fbear.mirror_companion.Spinner

@Composable
fun GuestScreenSettings(viewModel: CompanionViewModel = viewModel()) {

    val settings by viewModel.settings.observeAsState()

    val fonts by viewModel.fonts.observeAsState(emptyList())

    var guestHelloTextError by rememberSaveable { mutableStateOf(false) }
    var guestShootTextError by rememberSaveable { mutableStateOf(false) }
    var guestWaitTextError by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = settings?.guestHelloText ?: "",
            onValueChange = {
                viewModel.settings.value = viewModel.settings.value?.copy(guestHelloText = it)
                guestHelloTextError = it.isEmpty()
            },
            keyboardOptions = KeyboardOptions(autoCorrect = true, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            maxLines = 2,
            isError = guestHelloTextError,
            label = { Text(text = "Текст приветствия") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        OutlinedTextField(
            value = settings?.guestShootText ?: "",
            onValueChange = {
                viewModel.settings.value = viewModel.settings.value?.copy(guestShootText = it)
                guestShootTextError = it.isEmpty()
            },
            keyboardOptions = KeyboardOptions(autoCorrect = true, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            maxLines = 2,
            isError = guestShootTextError,
            label = { Text(text = "Текст съёмки") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        OutlinedTextField(
            value = settings?.guestWaitText ?: "",
            onValueChange = {
                viewModel.settings.value = viewModel.settings.value?.copy(guestWaitText = it)
                guestWaitTextError = it.isEmpty()
            },
            keyboardOptions = KeyboardOptions(autoCorrect = true, keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            maxLines = 2,
            isError = guestWaitTextError,
            label = { Text(text = "Текст ожидания фотографии") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )
        OutlinedTextField(
            value = settings?.guestShootTimer?.toString() ?: "",
            onValueChange = {
                viewModel.settings.value =
                    viewModel.settings.value?.copy(guestShootTimer = Regex("""\D""").replace(it, "").toIntOrNull())
            },
            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            maxLines = 1,
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
                viewModel.settings.value =
                    viewModel.settings.value?.copy(guestTextFontSize = Regex("""\D""").replace(it, "").toIntOrNull())
            },
            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            maxLines = 1,
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