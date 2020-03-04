package com.roomsandquality.rq

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.roompay.*
import kotlinx.android.synthetic.main.rooms.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import android.os.PersistableBundle


class roompay:AppCompatActivity() {
    var flag:Int = 0
    var count1 = 0
    var nin = 0
    var cnfrmbook = java.util.ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roompay)


        var roomseeker: String = entername.text.toString().trim()
        var phonenum:String = phonenum.text.toString().trim()
        var addr:String = addr.text.toString().trim()
        var transactionid:String = transactionid.text.toString().trim()

        cnfrmbook = intent.getStringArrayListExtra("cnfrmlist")!!
        checkusersign()

        signin.setOnClickListener {
            var myAnim: Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            signin.startAnimation(myAnim)
            if(flag == 2){
            var intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
            else{
                Toast.makeText(this,"Already Signed-In",Toast.LENGTH_LONG).show()
                signin.visibility = View.INVISIBLE
            }
        }

        transactbut.setOnClickListener {
            var myAnim: Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            transactbut.startAnimation(myAnim)
            if(!roomseeker.isEmpty()||!phonenum.isEmpty()||!addr.isEmpty()||!transactionid.isEmpty()){
                Toast.makeText(this,"Please Fill the Form",Toast.LENGTH_LONG).show()
            }

            else if(flag == 1){
                additemtosheet(cnfrmbook)
                nin = 1

            }

            if(nin == 1){
                val intent = Intent(this,finallybook::class.java)
                intent.putStringArrayListExtra("dispinfo",cnfrmbook)
                startActivity(intent)
            }
        }

    }


    private fun checkusersign() {
       val loading = ProgressDialog.show(this, "Signinig In", "Please Wait")
        val read = StringRequest(
            Request.Method.GET,
            "https://script.google.com/macros/s/AKfycbwjRDAoD9WXHmvmXnTh5WgOIzkDiddSr3pjDsGayht4rxM5HEE/exec?action=getItems",
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    parseitem(response!!)
                    loading.dismiss()
                    nin = 1
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(this@roompay, error?.message, Toast.LENGTH_LONG).show()
                }
            })

        val socketTimeout:Int=50000
        var retryPolicy: RetryPolicy = DefaultRetryPolicy(socketTimeout,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        read.setRetryPolicy(retryPolicy)
        val que = Volley.newRequestQueue(this)
        que.add<String>(read)
    }

    private fun parseitem(jsonresponse:String) {
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        var count:Int=0
        var i:Int=0
        var list:ArrayList<HashMap<String,String>> = ArrayList<HashMap<String,String>>()
        try {
            var job: JSONObject = JSONObject(jsonresponse)
            var jobarray: JSONArray = job.getJSONArray("items")
            while (i < jobarray.length()) {
                var jo: JSONObject = jobarray.getJSONObject(i)
                var uid: String = jo.getString("uid")
                var item: HashMap<String, String> = HashMap<String, String>()
                item.put("uid", uid)
                list.add(item)
                i = i + 1
            }
            count = i
            i = 0
        }
        catch (exp: JSONException){
            exp.printStackTrace()
        }
        while(i<count){
            if(list[i]["uid"] == user){
                flag = 1
                Toast.makeText(this,"Click to Confirm Booking",Toast.LENGTH_LONG).show()
                break
            }
            else{
                count1 = count1+1
            }
            i = i+1
        }
        if(count1== count){
            flag = 2
            Toast.makeText(this,"Please Sign-In",Toast.LENGTH_LONG).show()
        }
    }


    private fun additemtosheet(cnfrmbook: ArrayList<String>?) {
        val progressdia: ProgressDialog = ProgressDialog.show(this, "Adding Item", "Please Wait")
        var roomseeker: String = entername.text.toString().trim()
        var phonenum:String = phonenum.text.toString().trim()
        var addr:String = addr.text.toString().trim()
        var transactionid:String = transactionid.text.toString().trim()
        val req = object : StringRequest(
            Request.Method.POST,
            "https://script.google.com/macros/s/AKfycbw5j_ndV6iPFIjJO0zvFQRQSOoVx8Yj9GZSleQ7A4x_19gHVcE/exec",
            object: Response.Listener<String> {
                override fun onResponse(response: String?) {
                    progressdia.dismiss()
                    //Toast.makeText(applicationContext,response, Toast.LENGTH_LONG).show()
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(applicationContext, error?.message, Toast.LENGTH_LONG).show()
                }

            }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("action","addItem")
                params.put("Owner", cnfrmbook!![0])
                params.put("roomname",cnfrmbook[1])
                params.put("checkin",cnfrmbook[3])
                params.put("checkout",cnfrmbook[4])
                params.put("roomseeker",roomseeker)
                params.put("phonenum",phonenum)
                params.put("seekeraddr",addr)
                params.put("transactionid",transactionid)
                return params
            }
        }

        val socketTimeout:Int=50000
        var retryPolicy: RetryPolicy = DefaultRetryPolicy(socketTimeout,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        req.setRetryPolicy(retryPolicy)
        val que = Volley.newRequestQueue(this)
        que.add<String>(req)
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    lateinit var fa:Activity

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        fa = this
    }

}

