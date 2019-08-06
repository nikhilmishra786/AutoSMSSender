package com.infomantri.autosms.sender.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Message::class], version = 2)
abstract class MessageRoomDatabase: RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: MessageRoomDatabase? = null

        fun getDatabase(context: Context): MessageRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MessageRoomDatabase::class.java,
                    "message_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}