package com.example.timerapp.util

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.example.timerapp.*
import java.text.SimpleDateFormat
import java.util.*

class NotificationUtil {

    companion object{
        private const val CHANNEL_ID_TIMER="menu_timer"
        private const val CHANNEL_NAME_TIMER="Timer App Timer"
        private const val TIMER_ID=0

        fun showTimerExpired(context: Context)
        {
            val startIntent=Intent(context,TimerNotificationActionReceiver::class.java)
            startIntent.action=AppConstants.ACTION_START
            val pendingIntent=PendingIntent.getBroadcast(context,0,startIntent,PendingIntent.FLAG_UPDATE_CURRENT)


            val nBuilder=getBasicNotificationBuilder(context, CHANNEL_ID_TIMER,true)
            nBuilder.setContentTitle("Timer Expired!")
                .setContentText("Start Again?")
                .setContentIntent(getPendingIntentWithStack(context,TimerActivity::class.java))
                .addAction(R.drawable.ic_play_arrow_black_24dp,"Start",pendingIntent)

            val nManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)
            nManager.notify(TIMER_ID,nBuilder.build())
        }

        fun showTimerRunning(context: Context,wakeUpTime:Long)
        {
            val stopIntent=Intent(context,TimerNotificationActionReceiver::class.java)
            stopIntent.action=AppConstants.ACTION_STOP
            val stopPendingIntent=PendingIntent.getBroadcast(context,0,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val pauseIntent=Intent(context,TimerNotificationActionReceiver::class.java)
            pauseIntent.action=AppConstants.ACTION_PAUSE
            val pausePendingIntent=PendingIntent.getBroadcast(context,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val df=SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)



            val nBuilder=getBasicNotificationBuilder(context, CHANNEL_ID_TIMER,true)
            nBuilder.setContentTitle("Timer is Running")
                .setContentText("End: ${df.format(Date(wakeUpTime))}")
                .setContentIntent(getPendingIntentWithStack(context,TimerActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_stop_black_24dp,"Stop",stopPendingIntent)
                .addAction(R.drawable.ic_pause_black_24dp,"Pause",pausePendingIntent)

            val nManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)
            nManager.notify(TIMER_ID,nBuilder.build())
        }

        fun showTimerResumed(context: Context)
        {
            val resumeIntent=Intent(context,TimerNotificationActionReceiver::class.java)
            resumeIntent.action=AppConstants.ACTION_RESUME
            val pendingIntent=PendingIntent.getBroadcast(context,0,resumeIntent,PendingIntent.FLAG_UPDATE_CURRENT)


            val nBuilder=getBasicNotificationBuilder(context, CHANNEL_ID_TIMER,true)
            nBuilder.setContentTitle("Timer is Paused!!")
                .setContentText("Resume?")
                .setContentIntent(getPendingIntentWithStack(context,TimerActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_play_arrow_black_24dp,"Start",pendingIntent)

            val nManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)
            nManager.notify(TIMER_ID,nBuilder.build())
        }

        fun hideNotification(context: Context)
        {
            val nManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
        }

        private fun getBasicNotificationBuilder(context: Context, channelId: String, playSound: Boolean): NotificationCompat.Builder {
            val notificatinSound :Uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder=NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setDefaults(0)
            if(playSound) nBuilder.setSound(notificatinSound)

            return nBuilder

        }

        private fun <T> getPendingIntentWithStack(context: Context,javaClass:Class<T>):PendingIntent{
            val resultsIntent=Intent(context,javaClass)
            resultsIntent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val stackBuilder=TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultsIntent)
            return stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
        }
    @TargetApi(26)
        private fun NotificationManager.createNotificationChannel(channelId: String,channelName: String,playSound: Boolean){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            {
                val channelImportance=
                    if(playSound) NotificationManager.IMPORTANCE_DEFAULT
                    else
                        NotificationManager.IMPORTANCE_LOW
                val nChannel=NotificationChannel(channelId,channelName,channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor=Color.BLUE
                this.createNotificationChannel(nChannel)
            }

        }
    }
}