package com.roomsandquality.rq

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.payscreen.*
import java.io.IOException
import java.util.*

class payscreen:AppCompatActivity(){

    var smsManager = SmsManager.getDefault()
    private var read_msg_request = 1
    var cnfrmbook = java.util.ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payscreen)

        payscreenfun()

    }



    fun payscreenfun(){
        val curtime = Calendar.getInstance().time
        cnfrmbook = intent.getStringArrayListExtra("cnfrmlist")!!

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS()
        }

            paytm.setOnClickListener {
                val  i: Intent = getPackageManager().getLaunchIntentForPackage("net.one97.paytm")!!
                startActivity(i)
            }

        refresh.setOnClickListener {
            refreshSmsInbox(curtime)

        }

        contact.setOnClickListener {
            contact.text = "8171733710\n paras.2426@gmail.com"
        }

        process.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            //set title for alert dialog
            builder.setTitle(R.string.title)
            //set message for alert dialog
            builder.setMessage(R.string.process)

            //performing positive action
            builder.setPositiveButton("Ok"){dialogInterface, which ->
                Toast.makeText(applicationContext,"Have a Good Stay",Toast.LENGTH_LONG).show()
            }
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }

    fun getPermissionToReadSMS() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS),read_msg_request)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            read_msg_request -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("Recycle")
    fun refreshSmsInbox(time:Date) {
        var dateinms = ""
        var flag = 0
        var count = 0
        val contentResolver = contentResolver
        val smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
        val indexBody = smsInboxCursor!!.getColumnIndex("body")
        val indexAddress = smsInboxCursor.getColumnIndex("address")
        val indexdate = smsInboxCursor.getColumnIndex("date")
        if (indexBody < 0 || !smsInboxCursor.moveToFirst())
            Toast.makeText(this,"No messages",Toast.LENGTH_SHORT).show()

        do {
            if(/*smsInboxCursor.moveToFirst() &&*/ smsInboxCursor.getString(indexAddress).equals("BP-iPaytm") && smsInboxCursor.getString(indexBody).contains( cnfrmbook[2]+" transferred to 8535029926" , true)) {
                dateinms = smsInboxCursor.getString(indexdate)
                flag = 1
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dateinms.toLong()
                val date = calendar.time
                if(time.before(date)){
                    try {
                        val Paymentfinal = Intent(this, roompay::class.java)
                        Paymentfinal.putExtra("cnfrmlist", cnfrmbook)
                        startActivity(Paymentfinal)
                        finish()
                    }
                    catch(exp:IOException){
                        Toast.makeText(this,"Please Install Paytm App to Continue",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this,"Payment not Received.\n Please Contact Us.",Toast.LENGTH_SHORT).show()
                }
                break
            }
            count = count+1
        } while (smsInboxCursor.moveToNext() && count<3)

        if(flag == 0) {
            Toast.makeText(this, "We Cannot Process Your Request, Please Contact Us.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

}