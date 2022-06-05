package ru.fbear.mirror_companion

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.validator.routines.InetAddressValidator
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL


class StoreIpAddress(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("SETTINGS")
        val PREFERENCE_VALUE_NAME = stringPreferencesKey("ip_address")
    }

    val getIpAddress: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PREFERENCE_VALUE_NAME] ?: ""
        }

    suspend fun setIpAddress(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PREFERENCE_VALUE_NAME] = name
        }
    }


}

@Composable
fun ConnectionSettings(viewModel: CompanionViewModel = viewModel(), onFinish: () -> Unit) {
    val dataStore = StoreIpAddress(LocalContext.current)
    val address by dataStore.getIpAddress.collectAsState("")
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        IpConnectionTextField(
            address = address,
            onValueChange = { coroutineScope.launch { dataStore.setIpAddress(it) } },
            checkConnection = {
                withContext(Dispatchers.IO) {
                    if (InetAddress.getByAddress(address.split(".").map { it.toInt().toByte() }.toByteArray())
                            .isReachable(5000)
                    ) {
                        val url = URL("http://$address:8080/api/get/check/connection")
                        val conn = (url.openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                        }
                        try {
                            conn.responseCode == HttpURLConnection.HTTP_OK
                        } catch (e: ConnectException) {
                            false
                        }
                    } else false
                }
            },
            onConnectionSuccess = {
                coroutineScope.launch {
                    withContext(Dispatchers.Main) {
                        viewModel.initConnection(address)
                    }
                    onFinish()
                }
            },
            label = { Text(text = "Адрес зеркала") },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }

}

@Composable
fun IpConnectionTextField(
    address: String,
    onValueChange: (String) -> Unit,
    checkConnection: suspend (String) -> Boolean,
    onConnectionSuccess: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    var trailingIcon by remember { mutableStateOf<@Composable () -> Unit>({}) }
    val inetAddressValidator by remember { mutableStateOf(InetAddressValidator.getInstance()) }
    val coroutineScope = rememberCoroutineScope()
    var connectionIsChecking by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = address,
        onValueChange = onValueChange,
        trailingIcon = trailingIcon,
        isError = !inetAddressValidator.isValid(address),
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            autoCorrect = false,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            if (connectionIsChecking) return@KeyboardActions
            coroutineScope.launch {
                println("launched")
                connectionIsChecking = true
                if (inetAddressValidator.isValid(address)) {
                    trailingIcon = { CircularProgressIndicator() }
                    if (checkConnection(address)) {
                        trailingIcon = { Icon(Icons.Filled.Done, null) }
                        delay(1000L)
                        onConnectionSuccess()
                    } else {
                        trailingIcon = { Icon(Icons.Filled.Close, null) }
                    }
                }
                connectionIsChecking = false
            }
        }),
        placeholder = { Text(text = "10.0.0.100") },
        label = label,
        modifier = modifier
    )
}
