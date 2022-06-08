package ru.fbear.mirror_companion

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class MirrorObserverWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    override suspend fun doWork(): Result {
        val address = inputData.getString(ADDRESS_KEY) ?: return Result.failure()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$address:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(PhotoMirrorApi::class.java)
        val title = applicationContext.applicationInfo.nonLocalizedLabel

        var notificationId = try {
            api.getLastEventId().execute().body() ?: return Result.retry()
        } catch (e: IOException) {
            return Result.retry()
        }

        setForeground(createForegroundInfo("Наблюдение запущено", address))

        val resultIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(ADDRESS_KEY, address)
        }
        val resultPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, resultIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
        )

        val groupId = -address.split(".").last().toInt()

        while (!isStopped) {
            try {
                api.getEvents(notificationId).execute().body()?.let {
                    it.forEach { event ->
                        val groupNotification =
                            NotificationCompat.Builder(applicationContext, PHOTOMIRROR_STATE_CHANNEL_ID)
                                .setContentTitle(title)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setSubText("События $address")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setGroup(address)
                                .setGroupSummary(true)
                                .build()

                        val notification = NotificationCompat.Builder(applicationContext, PHOTOMIRROR_STATE_CHANNEL_ID)
                            .setContentTitle(title)
                            .setContentText(event.subject)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setSubText(address)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setGroup(address)
                            .setContentIntent(resultPendingIntent)
                            .setStyle(NotificationCompat.BigTextStyle().setSummaryText(event.subject).bigText(event.reason))
                            .build()
                        notificationManager.notify(notificationId, notification)
                        notificationManager.notify(groupId, groupNotification)

                        notificationId++
                    }
                } ?: return Result.retry()

            } catch (_: IOException) {
            }
            delay(1000L)
        }
        return Result.success()
    }

    private fun createForegroundInfo(text: String, address: String): ForegroundInfo {
        val title = applicationContext.applicationInfo.nonLocalizedLabel
        val cancel = "Выключить"
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val notificationId = -address.split(".").last().toInt() * 1000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, PHOTOMIRROR_STATE_CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(text)
            .setSilent(true)
            .setSubText(address)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val name = "Состояние фотозеркала"
        val descriptionText = "Уведомление о состоянии фотозеркала"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(PHOTOMIRROR_STATE_CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableVibration(true)
            enableLights(true)
            lightColor = Color.RED
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val PHOTOMIRROR_STATE_CHANNEL_ID = "PHOTOMIRROR_STATE_CHANNEL_ID"
        const val ADDRESS_KEY = "ADDRESS_KEY"
    }
}
