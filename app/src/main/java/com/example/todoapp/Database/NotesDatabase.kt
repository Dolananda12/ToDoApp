package com.example.todoapp.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [NotesEntitiy::class], version = 1, exportSchema = true )
@TypeConverters(Converters::class)
abstract class NotesDatabase: RoomDatabase(){
    abstract val dao: NotesInterface
    companion object{
        private var INSTANCE: NotesDatabase?=null
        fun getInstance(context: Context):NotesDatabase{
            synchronized(this){
                var instance= INSTANCE
                if(instance==null){
                    instance= Room.databaseBuilder(
                        context.applicationContext,
                        NotesDatabase::class.java,
                        "Notes_data_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE=instance
                }
                return  instance
            }
        }
    }
}