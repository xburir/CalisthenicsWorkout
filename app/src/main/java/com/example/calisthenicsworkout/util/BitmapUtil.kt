package com.example.calisthenicsworkout.util

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.*
import java.util.*
import kotlin.math.roundToInt

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

        suspend fun getBitmap(source: Uri, context: Context): Bitmap {
            val loading = ImageLoader(context)
            val request = ImageRequest.Builder(context).data(source).build()
            val result = (loading.execute(request) as SuccessResult).drawable
            return (result as BitmapDrawable).bitmap
        }

        fun compressBitmap(bmp: Bitmap, quality: Int): Bitmap{
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, quality, stream)
            val byteArray = stream.toByteArray()
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }


        fun resize(bmp: Bitmap, multiplier: Double): Bitmap{
            return Bitmap.createScaledBitmap(bmp, (bmp.width*multiplier).roundToInt(),(bmp.height*multiplier).roundToInt(), false)
        }


        fun saveToInternalStorage(bitmap: Bitmap, context: Context,imageId: String):Uri{
            val wrapper = ContextWrapper(context)
            var file = wrapper.getDir("images", Context.MODE_PRIVATE)
            file = File(file, "$imageId.png")
            try {
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException){ // Catch the exception
                e.printStackTrace()
            }
            return Uri.parse(file.path)
        }




    }
}