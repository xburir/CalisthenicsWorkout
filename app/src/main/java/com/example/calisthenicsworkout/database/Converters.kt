package com.example.calisthenicsworkout.database

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray{
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    @TypeConverter
    fun fromUri(uri: Uri): String{
        return uri.toString()
    }

    @TypeConverter
    fun toUri(str: String): Uri {
        return Uri.parse(str)
    }

    @TypeConverter
    fun fromArrayList(list: ArrayList<String?>): String{
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toArrayList(string: String): ArrayList<String>{
        val listType = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(string, listType)
    }

}