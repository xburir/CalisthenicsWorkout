package com.example.calisthenicsworkout.util

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.system.measureTimeMillis

class InternetUtil {
    companion object{


        suspend fun checkSpeed(): Boolean {
            val file = File.createTempFile("pic", "png")
            val time = measureTimeMillis {
                FirebaseStorage.getInstance().reference.child("measure.png").getFile(file).await()
            }
            val seconds = time.toDouble() / 1000

                        //kbytes per second
            return ((1064363 / seconds) / 1000 ) > 500

        }
    }
}