package com.example.timerapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.example.timerapp.util.NotificationUtil
import com.example.timerapp.util.PrefUtil

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class TimerActivity : AppCompatActivity() {


    companion object{
        fun setAlarm(context:Context,nowSeconds:Long,secondsRemaining:Long):Long{
            val wakeUpTime=(nowSeconds+secondsRemaining)*1000
            val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent=Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,0,intent,0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeUpTime,pendingIntent)
            PrefUtil.setAlarmSetTime(nowSeconds,context)

            return wakeUpTime

        }

        fun removeAlarm(context: Context){
            val intent=Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent=PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager=context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }

        val nowSeconds:Long
            get() = Calendar.getInstance().timeInMillis/1000
    }

    enum class TimerState{
        Stopped,Paused,Running
    }

    private lateinit var timer:CountDownTimer
    private var timerLengthSeconds=0L
    private var timerState=TimerState.Stopped
    private var secondsRemaining=0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.setTitle("    Timer")

        fab_play.setOnClickListener { v->
            startTimer()
            timerState=TimerState.Running
            updateButtons()
        }

        fab_pause.setOnClickListener { v->
            timer.cancel()
            timerState=TimerState.Paused
            updateButtons()
        }

        fab_stop.setOnClickListener { v->

            timer.cancel()
            onTimerFinished()
        }


    }

    override fun onResume() {
        super.onResume()
        initTimer()
        removeAlarm(this)
        NotificationUtil.hideNotification(this)
    }
    override fun onPause()
    {
        super.onPause()
        if(timerState==TimerState.Running) {
            timer.cancel()
            val wakeUpTime= setAlarm(this, nowSeconds,secondsRemaining)
            NotificationUtil.showTimerRunning(this,wakeUpTime)

        }
        else if(timerState==TimerState.Paused){
            NotificationUtil.showTimerResumed(this)
        }

        PrefUtil.setPreviousSecondsLength(timerLengthSeconds,this)
        PrefUtil.setSecondsRemaining(secondsRemaining,this)
        PrefUtil.setTimerState(timerState,this)
    }

    private fun initTimer(){
        timerState=PrefUtil.getTimerState(this)
        if(timerState==TimerState.Stopped)
        {
            setNewTimerLength()
        }
        else
        {
            setPreviousTimerLength()
        }

        secondsRemaining=if(timerState==TimerState.Paused || timerState==TimerState.Running)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds


        val alarmSetTime=PrefUtil.getAlarmSetTime(this)
        if(alarmSetTime>0){
            secondsRemaining-= nowSeconds-alarmSetTime
        }

        if(secondsRemaining<=0)
        {
            onTimerFinished()
        }
        else if(timerState==TimerState.Running)
            startTimer()
        updateButtons()
        updateCountDownUi()
    }

    private fun onTimerFinished()
    {
        timerState=TimerState.Stopped
        setNewTimerLength()
        progress_countdown.progress=0
        PrefUtil.setSecondsRemaining(timerLengthSeconds,this)
        secondsRemaining=timerLengthSeconds
        updateButtons()
        updateCountDownUi()
    }

    private fun startTimer()
    {
        timerState=TimerState.Running
        timer=object :CountDownTimer(secondsRemaining*1000,1000){
            override fun onFinish() =onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountDownUi()
            }
        }.start()
    }
    private fun setPreviousTimerLength()
    {
        timerLengthSeconds=PrefUtil.getPreviousSecondsLength(this)
        progress_countdown.max=timerLengthSeconds.toInt()
    }

    private fun setNewTimerLength()
    {
        val lengthInMinutes=PrefUtil.getTimerLength(this)
        timerLengthSeconds=(lengthInMinutes*60L)
        progress_countdown.max=timerLengthSeconds.toInt()
    }

    private fun updateCountDownUi()
    {
        val minutesUntilFinished=secondsRemaining/60
        val secondsInMinutesUntilFinished=secondsRemaining-minutesUntilFinished*60
        val secondsStr=secondsInMinutesUntilFinished.toString()
        text_countdown.text="$minutesUntilFinished:${
        if(secondsStr.length==2) secondsStr
        else "0"+secondsStr}"
        progress_countdown.progress=(timerLengthSeconds-secondsRemaining).toInt()
    }

    private fun updateButtons()
    {
        when(timerState){
            TimerState.Running->{
                fab_play.isEnabled=false
                fab_stop.isEnabled=true
                fab_pause.isEnabled=true
            }
            TimerState.Stopped->{
                fab_play.isEnabled=true
                fab_stop.isEnabled=false
                fab_pause.isEnabled=false
            }
            TimerState.Paused->{
                fab_play.isEnabled=true
                fab_stop.isEnabled=true
                fab_pause.isEnabled=false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent=Intent(this,SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
