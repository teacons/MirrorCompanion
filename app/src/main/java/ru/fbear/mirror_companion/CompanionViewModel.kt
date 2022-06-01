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

class CompanionViewModel : ViewModel() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://localhost/")    //todo: Вынести в настройки
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(PhotoMirrorApi::class.java)

    //    lateinit var printServices: MutableLiveData<List<String>>
    var printServices = MutableLiveData<List<String>>(emptyList())
    val printServiceMediaSizes = MutableLiveData(emptyList<String>())
    val printServiceLayouts = MutableLiveData(emptyList<String>())
    val printServicePreview = MutableLiveData<ImageBitmap>(null)

    val temp = Settings(
        "Камера 1",
        "Принтер 1",
        "10x15",
        true,
        "10.0.0.1",
        "layout",
        "Hello",
        "Shoot",
        "Wait",
        5,
        "image.png",
        "Times New Roman",
        100,
        0uL
    )

    //    lateinit var fonts: MutableLiveData<List<String>>
    var fonts = MutableLiveData<List<String>>(emptyList())

    //    lateinit var cameras: MutableLiveData<List<String>>
    var cameras = MutableLiveData<List<String>>(emptyList())

    //    lateinit var settings: MutableLiveData<Settings>
    var settings = MutableLiveData(temp)

    //    private lateinit var oldSettings: MutableLiveData<Settings>
    private var oldSettings = MutableLiveData(temp)

    init {
        api.getPrintServices().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                response.body()?.let { printServices = MutableLiveData(it) }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })

        api.getSettings().enqueue(object : Callback<Settings> {
            override fun onResponse(call: Call<Settings>, response: Response<Settings>) {
                response.body()?.let {
                    settings = MutableLiveData(it)
                    oldSettings = MutableLiveData(it)
                }
            }

            override fun onFailure(call: Call<Settings>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })

        api.getFonts().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                response.body()?.let { fonts = MutableLiveData(it) }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })

        api.getCameras().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                response.body()?.let { cameras = MutableLiveData(it) }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                TODO("Not yet implemented")
            }
        })

        settings.observeForever { settings ->
            when {
                settings.printerName != oldSettings.value!!.printerName -> {
                    api.getMediaSizeNames(settings.printerName).enqueue(object : Callback<List<String>> {
                        override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                            response.body()?.let { printServiceMediaSizes.value = it }
                        }

                        override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                            TODO("Not yet implemented")
                        }
                    })
                }
                settings.printerMediaSizeName != oldSettings.value!!.printerName -> {
                    api.getLayouts(settings.printerMediaSizeName, settings.printerName)
                        .enqueue(object : Callback<List<String>> {
                            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                                response.body()?.let { printServiceLayouts.value = it }
                            }

                            override fun onFailure(call: Call<List<String>>, t: Throwable) {
//                                TODO("Not yet implemented")
                            }
                        })
                }
                settings.layout != oldSettings.value!!.layout -> {
                    api.getLayoutWithPhoto(settings.layout, settings.printerMediaSizeName)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                response.body()?.let {
                                    printServicePreview.value =
                                        BitmapFactory.decodeByteArray(it.bytes(), 0, it.bytes().size).asImageBitmap()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                                TODO("Not yet implemented")
                            }

                        })
                }
            }
            oldSettings.value = settings
        }
    }

    fun checkInetAddress(inetAddress: String): Boolean {
        return api.checkInetAddress(inetAddress).execute().body() ?: false
    }
}