package com.roomsandquality.rq

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.finallybook.*
import kotlinx.android.synthetic.main.finallybook.contact
import kotlinx.android.synthetic.main.payscreen.*


class finallybook:AppCompatActivity(){

    lateinit var notificationmanager: NotificationManager
    lateinit var notificationchannel: NotificationChannel
    lateinit var builder: Notification.Builder
    val channelId = "inc.droidstar.notifications"
    val description="Rooms And Quality"
    var cnfrmbook = java.util.ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.finallybook)

        cnfrmbook = intent.getStringArrayListExtra("dispinfo")!!

        notificationmanager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notification()

        dispinfo.text = "Your "+ cnfrmbook[1]+" from "+ cnfrmbook[3]+" to "+ cnfrmbook[4]+"\nhas been successfully booked! \n Feel Free to Contact Us."

        addrbook.text = "Room Address: "+ cnfrmbook[5]
        contact.setOnClickListener {
            contact.text = "8171733710\n paras.2426@gmail.com"
        }

    }

    private fun notification(){
        val notifyIntent = Intent(this, notifyscr::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationchannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationchannel.enableLights(true)
            notificationchannel.lightColor = Color.GREEN
            notificationchannel.enableVibration(true)
            notificationmanager.createNotificationChannel(notificationchannel)

            builder = Notification.Builder(this,channelId)
                .setContentTitle("Booked Successfully")
                .setContentText(cnfrmbook[1]+" booked!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(Notification.BigTextStyle()
                    .bigText("Your "+ cnfrmbook[1]+" from "+ cnfrmbook[3]+" to "+ cnfrmbook[4]+"\nhas been successfully booked! \n ThankYou for Choosing Us"))
                .setContentIntent(notifyPendingIntent)
        }
        else{
            builder = Notification.Builder(this)
                .setContentTitle("Booked Successfully")
                .setContentText("Your "+ cnfrmbook[1]+" from "+ cnfrmbook[3]+" to "+ cnfrmbook[4]+"\nhas been successfully booked! \n ThankYou for Choosing Us")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(notifyPendingIntent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationmanager.notify(0,builder.build())
        }

    }

    override fun onBackPressed() {
        finish()
        roompay().fa.finish()
        super.onBackPressed()
    }
}