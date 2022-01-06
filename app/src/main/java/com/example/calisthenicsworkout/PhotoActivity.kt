package com.example.calisthenicsworkout

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.calisthenicsworkout.databinding.ActivityPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class PhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo)

        val folder = intent.getStringExtra("folder")
        val id = intent.getStringExtra("id")

        folder?.let{ folderString ->
            id?.let{ itId ->
                val localFile = File.createTempFile(folderString, "png")
                val fbStorageRef = FirebaseStorage.getInstance().reference.child(folderString).child("$itId.png").getFile(localFile)
                fbStorageRef
                    .addOnFailureListener {
                    binding.textView.text = "Picture not found"
                    binding.progressBar3.visibility = View.GONE
                }
                    .addOnCompleteListener {
                    if(it.isSuccessful){
                        binding.textView.visibility = View.GONE
                        binding.progressBar3.visibility = View.GONE
                        binding.imageView.visibility = View.VISIBLE
                        binding.imageView.setImageURI(Uri.parse(localFile.absolutePath))

                    }
                }
                    .addOnProgressListener {
                        val progress = (100*it.bytesTransferred/it.totalByteCount)
                        binding.progressBar3.progress = progress.toInt()
                        binding.textView.text = "$progress%"
                    }
            }



        }


    }
}