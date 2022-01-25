package com.smartgeeks.busticket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartgeeks.busticket.data.local.entities.TicketEntity

@Database(entities = [TicketEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ticketDao(): TicketDAO
}