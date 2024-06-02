package com.example.todoapp.Database
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    val gson = Gson()
    @TypeConverter
    fun fromTaskStructureList(taskStructureList: MutableList<TaskStructure>): String {
        return gson.toJson(taskStructureList)
    }

    @TypeConverter
    fun toTaskStructureList(taskStructureListString: String): MutableList<TaskStructure> {
        val type = object : TypeToken<MutableList<TaskStructure>>() {}.type
        return gson.fromJson(taskStructureListString, type)
    }
}