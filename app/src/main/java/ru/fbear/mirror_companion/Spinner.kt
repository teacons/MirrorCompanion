package ru.fbear.mirror_companion

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

interface Spinnable {
    override fun toString(): String
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Spinner(
    data: List<Spinnable>,
    value: String = "",
    onSelectedChanges: (Spinnable) -> Unit,
    label: @Composable () -> Unit = {},
    Content: @Composable (Spinnable) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded != expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = value,
            onValueChange = {},
            label = label,
            textStyle = MaterialTheme.typography.h6,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) { expanded = !expanded } },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            data.forEach {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectedChanges(it)
                    }
                ) {
                    Content(it)
                }
            }
        }
    }
}