package com.roomsandquality.rq

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class pro_scr:AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pro_scr)

        Handler().postDelayed(
            {
                val intent = Intent(this,rooms::class.java)
                startActivity(intent)
                this.finish()
            },4200
        )
    }
}