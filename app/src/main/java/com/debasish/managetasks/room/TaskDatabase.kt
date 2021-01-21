package com.debasish.managetasks.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.debasish.managetasks.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Tasks::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun tasksDao() : TasksDao

   /* companion object{
        @Volatile
        private var INSTANCE:TaskDatabase? = null
        fun getInstance(context: Context):TaskDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TaskDatabase::class.java,
                        "todo_database"
                    ).build()
                }
                return instance
            }
        }
    }*/

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().tasksDao()

        }
    }


}