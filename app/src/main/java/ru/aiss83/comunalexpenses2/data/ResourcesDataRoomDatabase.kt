package ru.aiss83.comunalexpenses2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

private const val DATABASE_NAME = "resources_data_database"

@Database(entities = [ResourceData::class], version = 1)
@TypeConverters(ResourcesDataTypesConverter::class)
abstract class ResourcesDataRoomDatabase: RoomDatabase() {
    abstract fun resourcesDataDao(): ResourceDataDao

    companion object {
        private var INSTANCE: ResourcesDataRoomDatabase? = null

        fun getInstance(context: Context): ResourcesDataRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(context = context.applicationContext,
                        ResourcesDataRoomDatabase::class.java,
                        DATABASE_NAME).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}