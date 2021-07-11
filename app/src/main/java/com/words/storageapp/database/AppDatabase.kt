package com.words.storageapp.database

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.words.storageapp.database.model.*
import com.words.storageapp.work.SeedDatabaseWorker

@Database(
    entities = [AllSkillsDbModel::class, AllSkillsFts::class,
        ClientDbModel::class, CommentDbModel::class, AddressModel::class,
        RecentSkillModel::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun allSkillsDbDao(): AllSkillsDbDao
    abstract fun clientDbDao(): ClientDbDao
    abstract fun commentDao(): CommentDao
    abstract fun addressDao(): AddressDao
    abstract fun recentDao(): RecentDao

    companion object {

        @Volatile
        var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "skills_db")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        //pre-populating the database with dummy data
                        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                        WorkManager.getInstance(context).enqueue(request)
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}