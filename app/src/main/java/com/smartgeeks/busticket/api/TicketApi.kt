package com.smartgeeks.busticket.api

import com.smartgeeks.busticket.data.ticket.ResponseSendSeatTicket
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TicketApi {

    @FormUrlEncoded
    @POST("api/setTicketAsientoNew")
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
        @Field("id_vehiculo") vehicleId: Int,
    ): ResponseSendSeatTicket
}