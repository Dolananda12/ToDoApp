package com.example.todoapp.Database
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract.CommonDataKinds.Note
import java.io.ByteArrayOutputStream
import java.util.Base64

class Converters {
    private val gson = Gson()
    @TypeConverter
    fun fromTaskStructureList(taskStructureList: MutableList<TaskStructure>): String {
        return gson.toJson(taskStructureList)
    }

    @TypeConverter
    fun toTaskStructureList(taskStructureListString: String): MutableList<TaskStructure> {
        val type = object : TypeToken<MutableList<TaskStructure>>() {}.type
        return gson.fromJson(taskStructureListString, type)
    }
    @TypeConverter
    fun fromNoteStructure(taskStructureList: NoteStructure): String {
        return gson.toJson(taskStructureList)
    }

    @TypeConverter
    fun toNoteStructure(taskStructureListString: String): NoteStructure {
        val type = object : TypeToken<NoteStructure>() {}.type
        return gson.fromJson(taskStructureListString, type)
    }
     @TypeConverter
    fun fromStringList(value: MutableList<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): MutableList<String> {
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    // Converter for MutableList<Pair<Double, String>>
    @TypeConverter
    fun fromStringStringPairList(value: MutableList<Pair<String, String>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringStringPairList(value: String): MutableList<Pair<String, String>> {
        val listType = object : TypeToken<MutableList<Pair<String, String>>>() {}.type
        return gson.fromJson(value, listType)
    }

}
