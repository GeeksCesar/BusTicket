package com.smartgeeks.busticket.data.local

import androidx.room.*
import com.smartgeeks.busticket.data.local.entities.TicketEntity

@Dao
interface TicketDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: TicketEntity) : Long

    @Update
    suspend fun update(ticket: TicketEntity)

    @Delete
    suspend fun delete(ticket: TicketEntity)

    @Query("SELECT * FROM ticket")
    suspend fun getAllTickets(): List<TicketEntity>

    @Query("SELECT * FROM ticket WHERE id = :id")
    suspend fun getTicketById(id: Int): TicketEntity

    @Query("SELECT COUNT(*) FROM ticket")
    suspend fun getCountTickets() : Int

}