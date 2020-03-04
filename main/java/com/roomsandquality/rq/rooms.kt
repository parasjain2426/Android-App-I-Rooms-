package com.roomsandquality.rq

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.rooms.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class rooms:AppCompatActivity() {
    var users= ArrayList<Upload>()
    private var recyclerView: RecyclerView? = null
    // private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.rooms)

            recyclerView = findViewById(R.id.recyclerview) as RecyclerView

            readsheettoitem()

            roomupload.setOnClickListener {
                val intent = Intent(this, roomUpload::class.java)
                startActivity(intent)
            }
    }

    private fun readsheettoitem() {
       // val loading = ProgressDialog.show(this, "Reading Item", "Please Wait")
        val read = StringRequest(
            Request.Method.GET,
            "https://script.google.com/macros/s/AKfycbxPoI59nflrUBt_ZxF4FEH_iRS_DhUFpDKKNFsvFIeOCqkwueU/exec?action=getItems",
            object : Response.Listener<String> {
                override fun onResponse(response: String?) {
                    parseitem(response!!)
                    bgban.setImageResource(R.drawable.rnq)
                    //loading.dismiss()
                }
            }, object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError?) {
                    Toast.makeText(this@rooms,"Internet Connectivity Slow!, Please restart the App" , Toast.LENGTH_LONG).show()
                }
            })
        //error?.message

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
                var NameId:String=jo.getString("Id")
                var Imgurl1:String=jo.getString("Imgurl1")
                var Imgurl2:String=jo.getString("Imgurl2")
                var phoneno:String=jo.getString("phoneno")
                var price:String=jo.getString("price")
                var roomname:String=jo.getString("roomname")
                var address:String=jo.getString("address")
                var verified:String = jo.getString("Verified")
                var item:HashMap<String,String> = HashMap<String,String>()
                item.put("NameId",NameId)
                item.put("Imgurl1",Imgurl1)
                item.put("Imgurl2",Imgurl2)
                item.put("phoneno",phoneno)
                item.put("price",price)
                item.put("roomname",roomname)
                item.put("address",address)
                item.put("Verified",verified)
                list.add(item)
                i=i+1
            }
            count=i
            i=0
        }
        catch (exp: JSONException){
            exp.printStackTrace()
        }

        while(i<count){
            if(list[i]["Verified"]=="1") {
                users.add(
                    Upload(
                        list[i]["NameId"].toString(),
                        list[i]["phoneno"].toString(),
                        list[i]["roomname"].toString(),
                        list[i]["address"].toString(),
                        list[i]["price"].toString(),
                        list[i]["Imgurl1"].toString(),
                        list[i]["Imgurl2"].toString()
                    )
                )
            }
            i=i+1
        }

        val adapter = roomsadapter(users,{ user : Upload -> ItemClicked(user) })
        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false) as RecyclerView.LayoutManager?
        recyclerView!!.adapter = adapter
    }

    private fun ItemClicked(user: Upload) {
        //Toast.makeText(this, "Clicked: ${user.Name}", Toast.LENGTH_LONG).show()
        val bookinfo = ArrayList<String>()
        bookinfo.add(user.Name)
        bookinfo.add(user.Nameofroom)
        bookinfo.add(user.Address)
        bookinfo.add(user.Price)
        bookinfo.add(user.Imgurl1)
        bookinfo.add(user.Imgurl2)
        // Launch second activity, pass part ID as string parameter
        val showDetailActivityIntent = Intent(this, roombook::class.java)
        showDetailActivityIntent.putExtra("infolist",bookinfo )
        startActivity(showDetailActivityIntent)
    }


    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

}