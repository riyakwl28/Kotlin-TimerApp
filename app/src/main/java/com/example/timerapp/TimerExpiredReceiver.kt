package com.example.timerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timerapp.util.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
       //TODO:SHOW NOTIFICATION
        PrefUtil.setTimerState(TimerActivity.TimerState.Stopped,context)
        PrefUtil.setAlarmSetTime(0,context)
    }
}
