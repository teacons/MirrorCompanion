package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.fbear.mirror_companion.CompanionViewModel
import ru.fbear.mirror_companion.IpConnectionTextField


@Composable
fun PhotoserverSettings(viewModel: CompanionViewModel = viewModel()) {

    val settings by viewModel.settings.observeAsState()

    var photoserverAddress by rememberSaveable { mutableStateOf(viewModel.settings.value?.photoserverAddress) }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = settings?.photoserverEnabled == true,
                onCheckedChange = {
                    viewModel.settings.value = viewModel.settings.value?.copy(photoserverEnabled = it)
                },
            )
            if (settings?.photoserverEnabled == true) {
                Text("Включен")
            } else {
                Text("Выключен")
            }
        }
        IpConnectionTextField(
            address = photoserverAddress ?: "",
            onValueChange = { photoserverAddress = it },
            checkConnection = { withContext(Dispatchers.IO){ photoserverAddress?.let { viewModel.checkInetAddress(it) } ?: false } },
            onConnectionSuccess = { viewModel.settings.value = viewModel.settings.value?.copy(photoserverAddress = photoserverAddress!!)},
            label = { Text(text = "Адрес фотосервера") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}