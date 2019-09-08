package com.infomantri.autosms.send.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Message::class, Subscribers::class, AddAlarm::class], version = 1, exportSchema = false)
abstract class MessageRoomDatabase: RoomDatabase() {

    abstract fun messageLiveDataDao(): MessageLivaDataDao
    abstract fun messageDbDao(): MessageDbDao
    abstract fun subscribersDao(): SubscribersLiveDataDao
    abstract fun addAlarmLiveDataDao(): AddAlarmLiveDataDao
    abstract fun addAlarmDao(): AddAlarmDao

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