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
        @RequiresApi(Build.VERSION_CODES.M)
        fun checkInternet(context: Context){

            // Connectivity Manager
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // Network Capabilities of Active Network
            val nc = cm.getNetworkCapabilities(cm.activeNetwork)

            val downSpeed = (nc?.linkDownstreamBandwidthKbps)?.div(1000)

            val uplSpeed = (nc?.linkUpstreamBandwidthKbps)?.div(1000)

            Toast.makeText(context,"Upload speed = ${downSpeed}MB/s \nDownload speed = ${uplSpeed}MB/s",Toast.LENGTH_LONG).show()


        }

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