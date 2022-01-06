package com.example.calisthenicsworkout.util

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import java.io.File

class BitmapUtil: Application(){


    companion object{

        fun getUri(bmp : Bitmap, quality: Int, context: Context): Uri {
            val file = File(context.cacheDir,"CUSTOM NAME")
            file.delete()
            file.createNewFile()
            val fileOutputStream = file.outputStream()
            val byteArrayOutputStream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG,quality,byteArrayOutputStream)
            val bytearray = byteArrayOutputStream.toByteArray()
            fileOutputStream.write(bytearray)
            fileOutputStream.flush()
            fileOutputStream.close()
            byteArrayOutputStream.close()
            return file.toUri()
        }

        fun compressBitmap(bmp: Bitmap, quality: Int): Bitmap{
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, quality, stream)
            val byteArray = stream.toByteArray()
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }





    }
}