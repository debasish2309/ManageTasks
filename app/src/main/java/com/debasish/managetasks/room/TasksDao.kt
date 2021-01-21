package com.debasish.managetasks.room

import androidx.room.*
import com.debasish.managetasks.room.Tasks
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(tasks: Tasks) : Long

    @Update
    suspend fun updateTask(tasks: Tasks) : Int

    @Delete
    suspend fun delete(tasks: Tasks)

    @Query("DELETE FROM table_todo WHERE id = :id")
    suspend fun deleteSingleTask(id: Int)

    @Query("DELETE FROM table_todo")
    suspend fun deleteAll():Int

    @Query("SELECT * FROM table_todo ORDER BY completed ASC, datecreated DESC")
    fun getAllTasks(): Flow<List<Tasks>>

    @Query("SELECT task FROM table_todo")
    suspend fun getOnlyTasks() : List<String>
}