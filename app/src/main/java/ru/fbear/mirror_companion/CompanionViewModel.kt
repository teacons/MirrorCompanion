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
import ru.fbear.mirror_companion.settings.Settings
import java.io.IOException

class CompanionViewModel : ViewModel() {
    private lateinit var retrofit: Retrofit

    private lateinit var api: PhotoMirrorApi

    val printServices = MutableLiveData<List<String>>(emptyList())
    val printServiceMediaSizes = MutableLiveData(emptyList<String>())
    val printServiceLayouts = MutableLiveData(emptyList<String>())
    val printServicePreview = MutableLiveData<ImageBitmap?>(null)

    val fonts = MutableLiveData<List<String>>(emptyList())
    val cameras = MutableLiveData<List<String>>(emptyList())
    val settings = MutableLiveData<Settings>(null)

    private val oldSettings = MutableLiveData<Settings>(null)

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

        settings.observeForever { settings ->
            if (settings == null || oldSettings.value == null) {
                return@observeForever
            }
            if (settings.printerName != oldSettings.value!!.printerName || printServiceMediaSizes.value!!.isEmpty()) {
                update(Type.MediaSizeNames)
            }
            if (settings.printerMediaSizeName != oldSettings.value!!.printerMediaSizeName || printServiceLayouts.value!!.isEmpty()) {
                update(Type.Layouts)
            }
            if (settings.layout != oldSettings.value!!.layout || printServicePreview.value == null) {
                update(Type.LayoutWithPhoto)
            }
            oldSettings.value = settings
        }
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
        }
    }

    private fun updateSettings() {
        api.getSettings().enqueue(object : Callback<Settings> {
            override fun onResponse(call: Call<Settings>, response: Response<Settings>) {
                response.body()?.let {
                    oldSettings.value = it
                    settings.value = it
                }
            }

            override fun onFailure(call: Call<Settings>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })
    }

    private fun updateLayoutWithPhoto() {
        val settings = settings.value!!
        api.getLayoutWithPhoto(settings.layout, settings.printerMediaSizeName, settings.printerName)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    response.body()?.let {
                        printServicePreview.value =
                            BitmapFactory.decodeByteArray(it.bytes(), 0, it.bytes().size)?.asImageBitmap()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                                TODO("Not yet implemented")
                }

            })
    }

    private fun updateWithReturnTypeListOfString(type: Type) {
        val settings = settings.value
        val callback = object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                response.body()?.let {
                    when (type) {
                        Type.MediaSizeNames -> printServiceMediaSizes.value = it
                        Type.Layouts -> printServiceLayouts.value = it
                        Type.PrintServices -> printServices.value = it
                        Type.Fonts -> fonts.value = it
                        Type.Cameras -> cameras.value = it
                        else -> throw IllegalArgumentException("Impossible")
                    }
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                            TODO("Not yet implemented")
            }
        }

        when (type) {
            Type.MediaSizeNames -> api.getMediaSizeNames(settings!!.printerName).enqueue(callback)
            Type.Layouts -> api.getLayouts(settings!!.printerMediaSizeName, settings.printerName).enqueue(callback)
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
}

enum class Type {
    MediaSizeNames,
    Layouts,
    PrintServices,
    Fonts,
    Cameras,
    LayoutWithPhoto,
    Settings
}