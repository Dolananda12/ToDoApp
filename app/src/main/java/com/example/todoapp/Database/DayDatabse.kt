package com.example.todoapp.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DayEntitiy::class], version = 1, exportSchema = true )
@TypeConverters(Converters::class)
abstract class DayDatabse :RoomDatabase(){
    abstract val dao: DayDAO
    companion object{
        @Volatile
        private var INSTANCE: DayDatabse?=null
        fun getInstance(context: Context):DayDatabse{
            synchronized(this){
                var instance= INSTANCE
                if(instance==null){
                    instance= Room.databaseBuilder(
                        context.applicationContext,
                        DayDatabse::class.java,
                        "Day_data_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE=instance
                }
                return  instance
            }
        }
    }
}