package com.smartgeeks.busticket.data.api

import com.smartgeeks.busticket.data.local.entities.TicketEntity
import com.smartgeeks.busticket.data.models.DefaultResponse
import com.smartgeeks.busticket.data.models.ticket.ResponseGetTicket
import com.smartgeeks.busticket.data.models.ticket.ResponseSaveTicket
import com.smartgeeks.busticket.data.models.ticket.ResponseSendSeatTicket
import com.smartgeeks.busticket.data.models.ticket.UpdateTicketPayload
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TicketApi {

    @FormUrlEncoded
    @POST("api/saveTicketAsientoV2")
    suspend fun saveSeatTicket(
        @Field("id_paradero_inicio") idStartRoute: Int,
        @Field("id_paradero_fin") idEndRoute: Int,
        @Field("id_ruta") routeId: Int,
        @Field("id_operador") operatorId: Int,
        @Field("hora") hour: String,
        @Field("id_tipo_usuario") userType: Int,
        @Field("total_pagar") totalCost: Double,
        @Field("sillas") seatsList: String,
        @Field("id_empresa") companyId: Int,
        @Field("id_vehiculo") vehicleId: Int, // Only necessary for voucher
        @Field("fecha") fecha: String = "",
        @Field("id_servicio") idServicio: Int = 0,
        @Field("tipo_ticket") tipoTicket: String = "SoloIda"
    ): ResponseSendSeatTicket

    @POST("apisync/saveTicketV2")
    suspend fun saveTicket(
        @Body ticketEntity: TicketEntity
    ): ResponseSaveTicket

    @POST("apisync/updateTicket")
    suspend fun updateTicket(
        @Body ticketUpdate: UpdateTicketPayload
    ): DefaultResponse

    @GET("apisync/searchTicketByVoucher")
    suspend fun searchTicketByVoucher(
        @Query("voucher_code") voucher: String
    ): ResponseGetTicket
}