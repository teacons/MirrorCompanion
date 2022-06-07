package ru.fbear.mirror_companion.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.fbear.mirror_companion.CompanionViewModel

@Composable
fun Control(viewModel: CompanionViewModel = viewModel()) {

    val isLocked by viewModel.mirrorIsLocked.observeAsState(false)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isLocked) "Заблокирован" else "Разблокирован",
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { viewModel.lockMirror(!isLocked) }
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = null,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Выключить зеркало",
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { viewModel.shutdownMirror() }
            ) {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = null,
                )
            }
        }
    }
}