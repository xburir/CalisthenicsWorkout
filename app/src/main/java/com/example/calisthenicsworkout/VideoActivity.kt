package com.example.calisthenicsworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class VideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val fbStorage = FirebaseStorage.getInstance()
        val skillId = intent.getStringExtra("skillId")
        val videoHolder = findViewById<VideoView>(R.id.videoView)
        val videoRef = fbStorage.reference.child("skillVideos").child("$skillId.mp4")
        videoRef.downloadUrl
            .addOnSuccessListener {
                videoHolder.setVideoURI(it)
                val mediaController = MediaController(this)
                mediaController.setAnchorView(videoHolder)
                videoHolder.setMediaController(mediaController)
                videoHolder.start()
            }
            .addOnFailureListener {
                Toast.makeText(this,"video not found",Toast.LENGTH_SHORT).show()
            }


    }
}