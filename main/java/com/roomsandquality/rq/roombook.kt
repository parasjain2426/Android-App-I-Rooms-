package com.roomsandquality.rq

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.roombook.*
import kotlinx.android.synthetic.main.roombook.imageView
import kotlinx.android.synthetic.main.roombook.price
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.Checksum
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class roombook:AppCompatActivity() {

    var bookinfo = ArrayList<String>()
    var cnfrmbook = java.util.ArrayList<String>()
    var cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roombook)
        bookinfo = intent.getStringArrayListExtra("infolist")

        roomname.text = bookinfo[1]
        Address.text = bookinfo[2]
        price.text = bookinfo[3]
        Picasso.get().load(bookinfo[4]).into(imageView)
        Picasso.get().load(bookinfo[5]).into(imageView1)

        book.visibility = View.INVISIBLE

        availchck.setOnClickListener {
            var myAnim: Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            availchck.startAnimation(myAnim)

            if (checkin.text.toString().equals("CheckIn") || checkout.text.toString().equals("CheckOut")) {
                Toast.makeText(this, "Please Choose the Check In-Out Dates", Toast.LENGTH_LONG)
                    .show()
            } else {
                readsheettoitem()
            }
        }

        shwaddr.setOnClickListener {
            var myAnim:Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            shwaddr.startAnimation(myAnim)
            val mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(Address.text.toString()))
            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }

        book.setOnClickListener {
            var myAnim:Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            book.startAnimation(myAnim)
            if (checkin.text.toString().equals("CheckIn") || checkout.text.toString().equals("CheckOut")) {
                Toast.makeText(this, "Please Choose the Check In-Out Dates", Toast.LENGTH_LONG)
                    .show()
            } else {

                cnfrmbook.add(bookinfo[0])
                cnfrmbook.add(bookinfo[1])
                cnfrmbook.add(bookinfo[3])
                cnfrmbook.add(checkin!!.text.toString())
                cnfrmbook.add(checkout!!.text.toString())
                cnfrmbook.add(bookinfo[2])

                val Paymentproceed = Intent(this, payscreen::class.java)
                Paymentproceed.putExtra("cnfrmlist", cnfrmbook)
                startActivity(Paymentproceed)
                finish()
            }
        }

    val dateSetListener1 = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(
            view: DatePicker, year: Int, monthOfYear: Int,
            dayOfMonth: Int
        ) {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView1()
        }
    }

    val dateSetListener2 = object : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(
            view: DatePicker, year: Int, monthOfYear: Int,
            dayOfMonth: Int
        ) {
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView2()
        }
    }

     checkin!!.setOnClickListener(object : View.OnClickListener {
         override fun onClick(view: View) {
             DatePickerDialog(this@roombook,
                 dateSetListener1,
                 // set DatePickerDialog to point to today's date when it loads up
                 cal.get(Calendar.YEAR),
                 cal.get(Calendar.MONTH),
                 cal.get(Calendar.DAY_OF_MONTH)).show()
         }

     })

     checkout!!.setOnClickListener(object : View.OnClickListener {
         override fun onClick(view: View) {
             DatePickerDialog(this@roombook,
                 dateSetListener2,
                 // set DatePickerDialog to point to today's date when it loads up
                 cal.get(Calendar.YEAR),
                 cal.get(Calendar.MONTH),
                 cal.get(Calendar.DAY_OF_MONTH)).show()
         }

     })
 }

    private fun updateDateInView1() {
        val myFormat = "ddMMyyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        checkin!!.text = sdf.format(cal.getTime())
    }

    private fun updateDateInView2() {
        val myFormat = "ddMMyyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        checkout!!.text = sdf.format(cal.getTime())
    }


    private fun readsheettoitem() {
        val loading = ProgressDialog.show(this, "Checking Availability", "Please Wait")
        val read = StringRequest(
            Request.Method.GET,
            "https://script.google.com/macros/s/AKfycbw5j_ndV6iPFIjJO0zvFQRQSOoVx8Yj9GZSleQ7A4x_19gHVcE/exec?action=getItems",
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    parseitem(response!!)
                    loading.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(this@roombook, error?.message, Toast.LENGTH_LONG).show()
                }
            })

        val socketTimeout:Int=50000
        var retryPolicy: RetryPolicy = DefaultRetryPolicy(socketTimeout,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        read.setRetryPolicy(retryPolicy)
        val que = Volley.newRequestQueue(this)
        que.add<String>(read)
    }

    private fun parseitem(jsonresponse:String) {
        var count:Int=0
        var i:Int=0
        var list:ArrayList<HashMap<String,String>> = ArrayList<HashMap<String,String>>()
        try {
            var job: JSONObject = JSONObject(jsonresponse)
            var jobarray: JSONArray =job.getJSONArray("items")
            while (i<jobarray.length()){
                var jo: JSONObject =jobarray.getJSONObject(i)
                var Owner:String=jo.getString("Owner")
                var roomname:String=jo.getString("roomname")
                var checkin:String=jo.getString("checkin")
                var checkout:String=jo.getString("checkout")
                var item:HashMap<String,String> = HashMap<String,String>()
                item.put("Owner",Owner)
                item.put("roomname",roomname)
                item.put("checkin",checkin)
                item.put("checkout",checkout)
                list.add(item)
                i=i+1
            }
            count=i
            i=0
        }
        catch (exp: JSONException){
            exp.printStackTrace()
        }
        try {
            var flag = 0
            val enteredcheckin =
                SimpleDateFormat("ddMMyyyy", Locale.getDefault()).parse(checkin.text.toString())
            val enteredcheckout =
                SimpleDateFormat("ddMMyyyy", Locale.getDefault()).parse(checkout.text.toString())
            val curdate = Calendar.getInstance().time

            while (i < count) {
                var crctdatein = list[i]["checkin"]
                var crctdateout = list[i]["checkout"]
                if(crctdatein!!.length == 7){
                    crctdatein = "0"+crctdatein
                }
                if(crctdateout!!.length == 7){
                    crctdateout = "0"+crctdateout
                }
                val checkindate =
                    SimpleDateFormat("ddMMyyyy", Locale.getDefault()).parse(crctdatein)
                val checkoutdate =
                    SimpleDateFormat("ddMMyyyy", Locale.getDefault()).parse(crctdateout)
                if (list[i]["Owner"].equals(bookinfo[0]) && list[i]["roomname"].equals(bookinfo[1])) {
                    if ((enteredcheckin!!.before(curdate)&&enteredcheckout!!.before(curdate))
                        ||((enteredcheckin.after(checkindate) && (enteredcheckin.before(checkoutdate))) && enteredcheckout!!.after(
                            checkoutdate))
                        || (enteredcheckin.before(checkindate) && (enteredcheckout!!.before(
                            checkoutdate) && (enteredcheckout.after(checkindate))))
                        || (enteredcheckin.after(checkindate) && enteredcheckout!!.before(
                            checkoutdate))
                        || (enteredcheckin.before(checkindate) && enteredcheckout!!.after(
                            checkoutdate))
                        || enteredcheckin.equals(checkindate) || enteredcheckout!!.equals(
                            checkoutdate)) {
                        Toast.makeText(
                            this,
                            "Room Not Available Choose Other Dates",
                            Toast.LENGTH_LONG
                        ).show()
                        flag = 1
                        book.visibility = View.INVISIBLE
                        break
                    }
                }

                i = i + 1
            }

            if (flag == 0) {
                Toast.makeText(
                    this,
                    "Room is Available, Click Book Now to Proceed.",
                    Toast.LENGTH_LONG
                ).show()
                book.visibility = View.VISIBLE
            }
        }
        catch (e: IOException){
            e.printStackTrace()
            Toast.makeText(
                this,
                "Room is Available, Click Book Now to Proceed.",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}

