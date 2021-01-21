package com.debasish.managetasks.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_todo")
data class Tasks(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    val id: Int = 0,
    @ColumnInfo(name = "task")
    val task : String,
    @ColumnInfo(name = "datecreated")
    val dateCreated: String,
    @ColumnInfo(name = "completed")
    val completed: Boolean
)
