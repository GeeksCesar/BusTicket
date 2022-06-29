package com.smartgeeks.busticket.data.models.ticket

data class ResponseSaveTicket(
    val mensaje : String = "",
    val estado : Int = 0,
    val id : Int = 0, // Id from local database (Used to delete the ticket)
    val remoto : Int = 0

)
