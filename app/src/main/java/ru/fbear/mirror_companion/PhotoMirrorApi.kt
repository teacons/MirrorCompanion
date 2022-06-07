package ru.fbear.mirror_companion

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.fbear.mirror_companion.settings.CameraConfigEntry
import ru.fbear.mirror_companion.settings.Settings


interface PhotoMirrorApi {
    @GET("/api/get/settings")
    fun getSettings(): Call<Settings>

    @GET("/api/get/print_services")
    fun getPrintServices(): Call<List<String>>

    @GET("/api/get/media_size_names")
    fun getMediaSizeNames(@Query("print_service", encoded = true) printService: String): Call<List<String>>

    @GET("/api/get/layouts")
    fun getLayouts(
        @Query("media_size_name", encoded = true) mediaSizeName: String,
        @Query("print_service", encoded = true) printService: String
    ): Call<List<String>>

    @GET("/api/get/layout_with_photos")
    fun getLayoutWithPhoto(
        @Query("layout", encoded = true) layout: String,
    ): Call<ResponseBody>

    @GET("/api/check/inet_address")
    fun checkInetAddress(@Query("inet_address", encoded = true) inetAddress: String): Call<Boolean>

    @GET("/api/get/fonts")
    fun getFonts(): Call<List<String>>

    @GET("/api/get/cameras")
    fun getCameras(): Call<List<String>>

    @GET("/api/get/camera/configs")
    fun getCameraConfigs(@Query("camera_name", encoded = true) cameraName: String): Call<List<CameraConfigEntry>>

    @POST("/api/post/settings")
    fun sendSettings(@Body settings: Settings): Call<ResponseBody>

    @POST("/api/post/camera_config")
    fun sendCameraConfiguration(@Body cameraConfiguration: List<CameraConfigEntry>): Call<ResponseBody>

    @POST("/api/post/lock")
    fun lockMirror(): Call<Boolean>

    @POST("/api/post/unlock")
    fun unlockMirror(): Call<Boolean>

    @POST("/api/post/shutdown")
    fun shutdownMirror(): Call<Boolean>
}