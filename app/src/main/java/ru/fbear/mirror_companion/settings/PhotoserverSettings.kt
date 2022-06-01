package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.validator.routines.InetAddressValidator
import ru.fbear.mirror_companion.CompanionViewModel

enum class AddressState {
    None, Good, Bad, Checking
}

@Composable
fun PhotoserverSettings(viewModel: CompanionViewModel = viewModel()) {

    val settings by viewModel.settings.observeAsState()

    var photoserverAddressError by rememberSaveable { mutableStateOf(false) }
    var photoserverAddressState by rememberSaveable { mutableStateOf(AddressState.None) }

    val coroutineScope = rememberCoroutineScope()

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
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = photoserverAddress ?: "",
                onValueChange = {
                    photoserverAddress = it
                    photoserverAddressError = !InetAddressValidator.getInstance().isValid(it)
                    photoserverAddressState = AddressState.None
                },
                maxLines = 1,
                isError = photoserverAddressError,
                label = { Text(text = "Адрес фотосервера") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    photoserverAddressState = AddressState.Checking
                    coroutineScope.launch(context = Dispatchers.IO) {
                        if (!photoserverAddressError) {
                            if (photoserverAddress?.let { viewModel.checkInetAddress(it) } == true) {
                                photoserverAddressState = AddressState.Good
                                viewModel.settings.value = viewModel.settings.value?.copy(photoserverAddress = photoserverAddress!!)
                            } else photoserverAddressState = AddressState.Bad
                        }
                    }
                }
            ) {
                Text(
                    text = "Проверить",
                )
            }
            Box(modifier = Modifier.size(40.dp)) {
                when (photoserverAddressState) {
                    AddressState.Good -> Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.matchParentSize()
                    )
                    AddressState.Bad -> Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.matchParentSize()
                    )
                    AddressState.Checking -> CircularProgressIndicator(
                        modifier = Modifier.matchParentSize()
                    )
                    AddressState.None -> {}
                }
            }
        }
    }
}