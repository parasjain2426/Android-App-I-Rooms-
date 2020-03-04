package com.roomsandquality.rq

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var read_msg_request = 1
    lateinit var mAuth: FirebaseAuth
    var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS()
            Toast.makeText(this,"Please Grant Permissions to Complete Payment",Toast.LENGTH_SHORT).show()
        }

        sendotp.setOnClickListener {
            var myAnim: Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.2, 15.0)
            myAnim.setInterpolator(interpolator)
            sendotp.startAnimation(myAnim)
            var number:String = phonetext.text.toString().trim()
            if((number.isEmpty())||(number.length<10)){
                Toast.makeText(this,"Enter Valid Phone Number", Toast.LENGTH_LONG).show()
            }
            else{
                verify(number)
                sendotp.text = "Please Wait"
                otpverify.setOnClickListener {
                    var myAnim:Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
                    var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.2, 15.0)
                    myAnim.setInterpolator(interpolator)
                    otpverify.startAnimation(myAnim)
                    authenticate()
                }
            }

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


    private fun verificationCallbacks () {
        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signIn(credential)
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Toast.makeText(applicationContext,"Verification Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verfication: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(verfication, p1)
                verificationId = verfication.toString()
            }

        }
    }

    private fun verify (phnNo:String) {

        verificationCallbacks()

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+91"+phnNo,
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks
        )
    }

    private fun signIn (credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                    task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    toast("Logged in Successfully :)")
                    adduser()
                    var intent = Intent(this,roompay::class.java)
                    startActivity(intent)
                }
            }
    }

    private fun adduser() {
        var user = FirebaseAuth.getInstance().currentUser!!.uid
        val req = object : StringRequest(
            Request.Method.POST,
            "https://script.google.com/macros/s/AKfycbwjRDAoD9WXHmvmXnTh5WgOIzkDiddSr3pjDsGayht4rxM5HEE/exec",
            object: Response.Listener<String> {
                override fun onResponse(response: String?) {
                    Toast.makeText(applicationContext,"Signing in Please Wait..", Toast.LENGTH_LONG).show()
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
                params.put("uid",user)
                return params
            }
        }

        val socketTimeout:Int=50000
        var retryPolicy: RetryPolicy = DefaultRetryPolicy(socketTimeout,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        req.setRetryPolicy(retryPolicy)
        val que = Volley.newRequestQueue(this)
        que.add<String>(req)
    }


    private fun authenticate () {

        val verifiNo = otptext.text.toString()

        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifiNo)

        signIn(credential)

    }

    private fun toast (msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }


    override fun onBackPressed() {
        super.onBackPressed()
       /* ActivityCompat.finishAffinity(this)*/
        var intent = Intent(this,roompay::class.java)
        startActivity(intent)
    }

}
