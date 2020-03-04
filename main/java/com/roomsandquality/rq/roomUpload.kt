package com.roomsandquality.rq

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.roomupload.*
import java.io.IOException

class roomUpload:AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 234

    var down1:Uri? = null
    var down2:Uri? = null

    //uri to store file
    private var filePath: Uri? = null

    var count:Int = 0

    //firebase objects
    private var storageReference: StorageReference? = null
    //private var mDatabase: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.roomupload)


        storageReference = FirebaseStorage.getInstance().getReference()
        /* mDatabase = FirebaseDatabase.getInstance().getReference(Constants().DATABASE_PATH_UPLOADS )*/

        buttonChoose.setOnClickListener{
            var myAnim:Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            buttonChoose.startAnimation(myAnim)
            showFileChooser()
        }
        buttonUpload.setOnClickListener{
            var myAnim:Animation = AnimationUtils.loadAnimation(this,R.anim.bounce)
            var interpolator:MyBounceInterpolator = MyBounceInterpolator(0.1, 5.0)
            myAnim.setInterpolator(interpolator)
            buttonUpload.startAnimation(myAnim)
            uploadFile()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }


    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    fun getFileExtension(uri: Uri): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    private fun uploadFile() {
        //checking if file is available
        var down: Uri
        if (filePath != null) {
            //displaying progress dialog while image is uploading
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading")
            progressDialog.show()

            //getting the storage reference
            val sRef = storageReference!!.child(
                Constants().STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(
                    filePath!!
                )
            )

            //adding the file to reference
            sRef.putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    //dismissing the progress dialog
                    progressDialog.dismiss()

                    //displaying success toast
                    Toast.makeText(applicationContext, "File Uploaded ", Toast.LENGTH_LONG).show()

                    sRef.downloadUrl.addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                        /*Log.e("Tuts+", "uri: $uri")*/
                        down = uri
                        //Handle whatever you're going to do with the URL here
                        triadditem(down)

                    })
                }
                .addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    //displaying the upload progress
                    val progress =
                        100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%...")
                }
        } else {
            //display an error if no file is selected
            Toast.makeText(applicationContext,"Please Choose an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triadditem(down:Uri){
        count = count+1
        if(count == 1){
            down1 = down
        }
        if(count==2){
            down2 = down
            buttonUpload.visibility = View.INVISIBLE
            var nameofroom = editText.text.toString().trim()
            var Name = name.text.toString().trim()
            var phoneno = phoneno.text.toString().trim()
            var price = price.text.toString().trim()
            var address = address.text.toString().trim()
            additemtosheet(down1!!,down2!!,nameofroom,Name,phoneno,price,address)
           /* var intent = Intent(this,rooms::class.java)
            startActivity(intent)*/
        }
    }

    private fun additemtosheet(
        Imgurl1: Uri,
        Imgurl2: Uri,
        nameofroom: String,
        name: String,
        phoneno: String,
        price: String,
        address: String
    ) {
        val progressdia: ProgressDialog = ProgressDialog.show(this, "Adding Item", "Please Wait")
        val req = object : StringRequest(
            Request.Method.POST,
            "https://script.google.com/macros/s/AKfycbxPoI59nflrUBt_ZxF4FEH_iRS_DhUFpDKKNFsvFIeOCqkwueU/exec",
            object: Response.Listener<String> {
                override fun onResponse(response: String?) {
                    progressdia.dismiss()
                    Toast.makeText(applicationContext,response, Toast.LENGTH_LONG).show()
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
                params.put("Name", name)
                params.put("Imgurl1",Imgurl1.toString())
                params.put("Imgurl2",Imgurl2.toString())
                params.put("phoneno", phoneno)
                params.put("price", price)
                params.put("roomname", nameofroom)
                params.put("address", address)
                params.put("Verified","0")
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
}