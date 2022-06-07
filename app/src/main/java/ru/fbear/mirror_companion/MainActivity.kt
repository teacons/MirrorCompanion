package ru.fbear.mirror_companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import ru.fbear.mirror_companion.settings.Settings
import ru.fbear.mirror_companion.ui.theme.MirrorCompanionTheme

enum class State {
    ConnectionSettings, MirrorSettings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MirrorCompanionTheme {
                Surface(color = MaterialTheme.colors.background) {
                    var state by rememberSaveable { mutableStateOf(State.ConnectionSettings) }
                    when (state) {
                        State.ConnectionSettings -> ConnectionSettings { state = State.MirrorSettings }
                        State.MirrorSettings -> Settings { state = State.ConnectionSettings }
                    }
                }
            }
        }
    }
}