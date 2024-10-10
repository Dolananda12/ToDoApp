package com.example.todoapp.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(exportSchema = true, entities = [TokenEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class TokenDatabase : RoomDatabase() {
    abstract val dao : TokenDAO
    companion object{
        private var INSTANCE : TokenDatabase?=null
        fun getInstance(context: Context): TokenDatabase{
        synchronized(this){
            var instance = INSTANCE
            if(instance==null){
                instance= Room.databaseBuilder(
                    context.applicationContext,
                    TokenDatabase::class.java,
                    "TokenDatabase"
                ).fallbackToDestructiveMigration().build()
                INSTANCE =instance
            }
            return instance
        }
    }
   }
}