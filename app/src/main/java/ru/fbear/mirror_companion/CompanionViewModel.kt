package ru.fbear.mirror_companion

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.fbear.mirror_companion.settings.CameraConfigEntry
import ru.fbear.mirror_companion.settings.Settings
import java.io.IOException

class CompanionViewModel : ViewModel() {
    private lateinit var retrofit: Retrofit

    private lateinit var api: PhotoMirrorApi

    var incomingChanges = MutableLiveData(false)

    val printServices = MutableLiveData<List<String>>(emptyList())
    val printServiceMediaSizes = MutableLiveData(emptyList<String>())
    val printServiceLayouts = MutableLiveData(emptyList<String>())
    val printServicePreview = MutableLiveData<ImageBitmap?>(null)

    val fonts = MutableLiveData<List<String>>(emptyList())
    val cameras = MutableLiveData<List<String>>(emptyList())
    val settings = MutableLiveData<Settings>(null)

    val cameraConfigs = MutableLiveData<List<CameraConfigEntry>>(emptyList())
    val oldCameraConfigs = MutableLiveData<List<CameraConfigEntry>>(emptyList())

    private val oldSettings = MutableLiveData<Settings>(null)

    var isRefreshingCameras = MutableLiveData(false)

    var isRefreshingPrinters = MutableLiveData(false)

    val mirrorIsLocked = MutableLiveData(false)

    val mirrorIsShutdown = MutableLiveData(false)

    fun initConnection(address: String) {
        retrofit = Retrofit.Builder()
            .baseUrl("http://$address:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(PhotoMirrorApi::class.java)

        update(Type.Settings)

        update(Type.PrintServices)

        update(Type.Fonts)

        update(Type.Cameras)

        mirrorIsLocked.value = false

        mirrorIsShutdown.value = false

        settings.observeForever { settings ->
            if (settings == null) {
                return@observeForever
            }
            if (settings != oldSettings.value && oldSettings.value != null) incomingChanges.value = true
            if (settings.cameraName != oldSettings.value?.cameraName || cameraConfigs.value!!.isEmpty()) {
                update(Type.CameraConfig)
            }
            if (settings.printerName != oldSettings.value?.printerName || printServiceMediaSizes.value!!.isEmpty()) {
                printServicePreview.value = null
                update(Type.MediaSizeNames)
            }
            if (settings.printerMediaSizeName != oldSettings.value?.printerMediaSizeName || printServiceLayouts.value!!.isEmpty()) {
                printServicePreview.value = null
                update(Type.Layouts)
            }
            if (settings.layout != oldSettings.value?.layout || printServicePreview.value == null) {
                printServicePreview.value = null
                update(Type.LayoutWithPhoto)
            }
            oldSettings.value = settings
        }

        cameraConfigs.observeForever {
            if (it.isNullOrEmpty()) return@observeForever
            if (it != oldCameraConfigs.value  && !oldCameraConfigs.value.isNullOrEmpty()) incomingChanges.value = true
            oldCameraConfigs.value = it
        }
    }

    fun refreshCameras() {
        isRefreshingCameras.value = true
        update(Type.Settings)
        update(Type.CameraConfig)
        update(Type.Cameras)
    }

    fun refreshPrinters() {
        isRefreshingPrinters.value = true
        update(Type.Settings)
        update(Type.PrintServices)
    }

    fun sendNewSettings() {
        api.sendSettings(settings.value!!).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                incomingChanges.value = false
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })
        sendCameraConfiguration()
    }

    private fun sendCameraConfiguration() {
        api.sendCameraConfiguration(cameraConfigs.value!!).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })
    }

    private fun update(type: Type) {
        when (type) {
            Type.MediaSizeNames -> updateWithReturnTypeListOfString(type)
            Type.Layouts -> updateWithReturnTypeListOfString(type)
            Type.PrintServices -> updateWithReturnTypeListOfString(type)
            Type.Fonts -> updateWithReturnTypeListOfString(type)
            Type.Cameras -> updateWithReturnTypeListOfString(type)
            Type.Settings -> updateSettings()
            Type.LayoutWithPhoto -> updateLayoutWithPhoto()
            Type.CameraConfig -> updateCameraConfig()
        }
    }

    private fun updateSettings() {
        api.getSettings().enqueue(object : Callback<Settings> {
            override fun onResponse(call: Call<Settings>, response: Response<Settings>) {
                response.body()?.let { settings.value = it }
            }

            override fun onFailure(call: Call<Settings>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })
    }

    private fun updateLayoutWithPhoto() {
        val settings = settings.value!!
        if (settings.layout != null && settings.printerMediaSizeName != null && settings.printerName != null) {
            api.getLayoutWithPhoto(settings.layout!!)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        response.body()?.let {
                            printServicePreview.value =
                                BitmapFactory.decodeStream(it.byteStream())?.asImageBitmap()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                                TODO("Not yet implemented")
                    }

                })
        }
    }

    private fun updateCameraConfig() {
        if (settings.value!!.cameraName != null) {
            api.getCameraConfigs(settings.value!!.cameraName!!).enqueue(object : Callback<List<CameraConfigEntry>> {
                override fun onResponse(
                    call: Call<List<CameraConfigEntry>>,
                    response: Response<List<CameraConfigEntry>>
                ) {
                    response.body()?.let { cameraConfigs.value = it }
                }

                override fun onFailure(call: Call<List<CameraConfigEntry>>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
        } else cameraConfigs.value = emptyList()
    }

    private fun updateWithReturnTypeListOfString(type: Type) {
        val settings = settings.value
        val callback = object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                response.body()?.let {
                    when (type) {
                        Type.MediaSizeNames -> printServiceMediaSizes.value = it
                        Type.Layouts -> printServiceLayouts.value = it
                        Type.PrintServices -> {
                            printServices.value = it
                            if (isRefreshingPrinters.value!!) isRefreshingPrinters.value = false
                        }
                        Type.Fonts -> fonts.value = it
                        Type.Cameras -> {
                            cameras.value = it
                            if (isRefreshingCameras.value!!) isRefreshingCameras.value = false
                        }
                        else -> throw IllegalArgumentException("Impossible")
                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                            TODO("Not yet implemented")
            }
        }

        when (type) {
            Type.MediaSizeNames ->
                if (settings!!.printerName != null) api.getMediaSizeNames(settings.printerName!!).enqueue(callback)
            Type.Layouts ->
                if (settings!!.printerName != null && settings.printerMediaSizeName != null)
                    api.getLayouts(settings.printerMediaSizeName!!, settings.printerName!!).enqueue(callback)
            Type.PrintServices -> api.getPrintServices().enqueue(callback)
            Type.Fonts -> api.getFonts().enqueue(callback)
            Type.Cameras -> api.getCameras().enqueue(callback)
            else -> throw IllegalArgumentException("Impossible")
        }
    }

    fun checkInetAddress(inetAddress: String): Boolean {
        return try {
            api.checkInetAddress(inetAddress).execute().body() ?: false
        } catch (e: IOException) {
            false
        }
    }

    fun updateCameraConfigValue(configName: String, configValue: String) {
        cameraConfigs.value = cameraConfigs.value!!.map {
            if (it.configName == configName) it.copy(value = configValue) else it
        }
    }

    fun lockMirror(lock: Boolean = true) {
        val callback = object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                response.body()?.let { mirrorIsLocked.value = it }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        }
        if (lock)
            api.lockMirror().enqueue(callback)
        else
            api.unlockMirror().enqueue(callback)
    }

    fun shutdownMirror() {
        api.shutdownMirror().enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                response.body()?.let { mirrorIsShutdown.value = it }
            }

            override fun onFailure(call: Call<Boolean>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })
    }
}

enum class Type {
    MediaSizeNames,
    Layouts,
    PrintServices,
    Fonts,
    Cameras,
    LayoutWithPhoto,
    Settings,
    CameraConfig
}