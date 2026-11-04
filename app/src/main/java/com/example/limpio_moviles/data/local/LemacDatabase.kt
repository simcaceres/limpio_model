package com.example.limpio_moviles.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RegistroEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LemacDatabase : RoomDatabase() {

    abstract fun registroDao(): RegistroDao

    companion object {
        @Volatile
        private var INSTANCE: LemacDatabase? = null

        fun getDatabase(context: Context): LemacDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LemacDatabase::class.java,
                    "lemac_datalab.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
