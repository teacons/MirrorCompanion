package ru.fbear.mirror_companion.settings

import Spinnable
import Spinner
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fbear.mirror_companion.CompanionViewModel


@Composable
fun PrinterSettings(viewModel: CompanionViewModel = viewModel()) {

    val printServices by viewModel.printServices.observeAsState(emptyList())

    val mediaSizes by viewModel.printServiceMediaSizes.observeAsState(emptyList())

    val layouts by viewModel.printServiceLayouts.observeAsState(emptyList())

    val layoutPreview by viewModel.printServicePreview.observeAsState()

    val settings by viewModel.settings.observeAsState(null)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Spinner(
            data = printServices.map {
                object : Spinnable {
                    override fun toString() = it
                }
            },
            value = settings?.printerName ?: "",
            onSelectedChanges = {
                viewModel.settings.value = viewModel.settings.value?.copy(printerName = it.toString())
            },
            label = { Text(text = "Принтер") }
        ) {
            Text(text = it.toString())
        }

        Spinner(
            data = mediaSizes.map {
                object : Spinnable {
                    override fun toString() = it
                }
            },
            value = settings?.printerMediaSizeName ?: "",
            onSelectedChanges = {
                viewModel.settings.value = viewModel.settings.value?.copy(printerMediaSizeName = it.toString())
            },
            label = { Text(text = "Размер бумаги") }
        ) {
            Text(text = it.toString())
        }

        Spinner(
            data = layouts.map {
                object : Spinnable {
                    override fun toString() = it
                }
            },
            value = settings?.layout ?: "",
            onSelectedChanges = {
                viewModel.settings.value = viewModel.settings.value?.copy(layout = it.toString())
            },
            label = { Text(text = "Совместимый макет") }
        ) {
            Text(text = it.toString())
        }

        if (layoutPreview != null) {
            Image(
                bitmap = layoutPreview!!,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .border(BorderStroke(2.dp, Color.Black))
            )
        } else CircularProgressIndicator()
    }
}