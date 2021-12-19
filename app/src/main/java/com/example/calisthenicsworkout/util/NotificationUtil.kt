package com.example.calisthenicsworkout.util

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.calisthenicsworkout.R
import com.example.calisthenicsworkout.TimerActivity
import java.text.SimpleDateFormat
import java.util.*

class NotificationUtil {
    companion object{
        private const val CHANNEL_ID_TIMER = "training_timer"
        private const val CHANNEL_NAME_TIMER = "CalisthenicsWorkout timer"
        private const val TIMER_ID = 0

        fun showTimerExpired(context: Context){
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Time expired!")
            nBuilder.setContentText("Time to train")
            //nBuilder.setContentIntent(getPendingIntentWithStack(context,TimerActivity::class.java))
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)
            nManager.notify(TIMER_ID,nBuilder.build())

        }

        fun hideTimerNotification(context: Context){
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
        }

        val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

        fun showTimerRunning(context: Context,wakeUpTime: Long){
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER, true)
            nBuilder.setContentTitle("Timer is running.")
            nBuilder.setContentText("End"+df.format(Date(wakeUpTime)))
            //nBuilder.setContentIntent(getPendingIntentWithStack(context,TimerActivity::class.java))
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)
            nManager.notify(TIMER_ID,nBuilder.build())

        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, sound: Boolean): NotificationCompat.Builder {
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
            if(sound)  nBuilder.setSound(notificationSound)
            return nBuilder
        }


        private fun <T> getPendingIntentWithStack(context: Context, javaClass: Class<T>): PendingIntent{
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        @SuppressLint("ObsoleteSdkInt")
        private fun NotificationManager.createNotificationChannel(channelId: String,
                                                                  channelName: String, sound: Boolean){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channelImportance = if(sound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelId,channelName,channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}