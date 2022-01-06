package com.example.calisthenicsworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.viewModelScope
import com.example.calisthenicsworkout.databinding.ActivityVideoBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.File

class VideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)


        val fbStorage = FirebaseStorage.getInstance()
        val skillId = intent.getStringExtra("skillId")
        val localFile = File.createTempFile("video", "mp4")

        fbStorage.reference.child("skillVideos").child("$skillId.mp4").getFile(localFile)
            .addOnFailureListener {
                binding.progressText.text = "Video not found"
                binding.progressBar2.visibility = View.GONE
            }
            .addOnCompleteListener {
                if(it.isSuccessful){
                    binding.progressText.visibility = View.GONE
                    binding.progressBar2.visibility = View.GONE
                    binding.videoView.visibility = View.VISIBLE

                    binding.videoView.setVideoPath(localFile.absolutePath)
                    val mediaController = MediaController(this)
                    mediaController.setAnchorView(binding.videoView)
                    binding.videoView.setMediaController(mediaController)
                    binding.videoView.start()
                }
            }
            .addOnProgressListener {
                val progress = (100*it.bytesTransferred/it.totalByteCount)
                binding.progressBar2.progress = progress.toInt()
                binding.progressText.text = "$progress%"
            }

    }
}