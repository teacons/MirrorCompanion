package ru.fbear.mirror_companion

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.work.*
import ru.fbear.mirror_companion.MirrorObserverWorker.Companion.ADDRESS_KEY
import ru.fbear.mirror_companion.settings.Settings
import ru.fbear.mirror_companion.ui.theme.MirrorCompanionTheme
import java.util.concurrent.TimeUnit


enum class State {
    ConnectionSettings, MirrorSettings
}

const val MIRROR_OBSERVER_TAG = "MirrorObserver"

class MainActivity : ComponentActivity() {

    private var address = ""

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        address = intent?.getStringExtra(ADDRESS_KEY) ?: ""
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = intent?.getStringExtra(ADDRESS_KEY) ?: ""
        val viewModel: CompanionViewModel by viewModels()

        val initState = if (address.isEmpty()) {
            State.ConnectionSettings
        } else {
            viewModel.initConnection(address)
            State.MirrorSettings
        }


        setContent {
            MirrorCompanionTheme {
                Surface(color = MaterialTheme.colors.background) {
                    var state by rememberSaveable { mutableStateOf(initState) }
                    val isConnectionError by viewModel.isConnectionError.observeAsState(false)

                    if (isConnectionError) {
                        state = State.ConnectionSettings
                        Toast.makeText(this, "Соединение потеряно", Toast.LENGTH_SHORT).show()
                        stopWorker(viewModel.address)
                    }

                    when (state) {
                        State.ConnectionSettings -> ConnectionSettings {
                            state = State.MirrorSettings
                            startWorker(viewModel.address)
                        }
                        State.MirrorSettings -> Settings {
                            state = State.ConnectionSettings
                        }
                    }
                }
            }
        }
    }

    private fun startWorker(address: String) {
        val data = Data.Builder().putString(ADDRESS_KEY, address).build()
        val worker =
            OneTimeWorkRequest.Builder(MirrorObserverWorker::class.java)
                .addTag(MIRROR_OBSERVER_TAG)
                .setInputData(data)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        WorkManager.getInstance(this).enqueueUniqueWork("MirrorObserver:$address", ExistingWorkPolicy.KEEP, worker)
    }

    private fun stopWorker(address: String) {
        WorkManager.getInstance(this).cancelUniqueWork("MirrorObserver:$address")
    }
}