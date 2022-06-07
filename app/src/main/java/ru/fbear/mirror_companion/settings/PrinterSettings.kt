package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.fbear.mirror_companion.CompanionViewModel
import ru.fbear.mirror_companion.Spinnable
import ru.fbear.mirror_companion.Spinner


@Composable
fun PrinterSettings(viewModel: CompanionViewModel = viewModel()) {

    val printServices by viewModel.printServices.observeAsState(emptyList())

    val mediaSizes by viewModel.printServiceMediaSizes.observeAsState(emptyList())

    val layouts by viewModel.printServiceLayouts.observeAsState(emptyList())

    val layoutPreview by viewModel.printServicePreview.observeAsState()

    val settings by viewModel.settings.observeAsState(null)

    val isRefreshingPrinters by viewModel.isRefreshingPrinters.observeAsState(false)

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshingPrinters),
        onRefresh = { viewModel.refreshPrinters() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Spinner(
                data = printServices.map {
                    object : Spinnable {
                        override fun toString() = it
                    }
                },
                value = settings?.printerName ?: "",
                onSelectedChanges = {
                    viewModel.settings.value = viewModel.settings.value?.copy(
                        printerName = it.toString(),
                        printerMediaSizeName = null,
                        layout = null
                    )
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
                    viewModel.settings.value = viewModel.settings.value?.copy(
                        printerMediaSizeName = it.toString(),
                        layout = null
                    )
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

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (layoutPreview != null) {
                    Image(
                        bitmap = layoutPreview!!,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (settings?.layout != null) CircularProgressIndicator()
            }
        }
    }
}