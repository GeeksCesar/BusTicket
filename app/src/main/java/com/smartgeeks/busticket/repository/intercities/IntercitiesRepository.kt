package com.smartgeeks.busticket.repository.intercities

import com.smartgeeks.busticket.data.models.intercities.HoursResponse
import com.smartgeeks.busticket.data.models.intercities.RouteIntercityResponse
import com.smartgeeks.busticket.data.models.intercities.StopBusResponse
import com.smartgeeks.busticket.domain.models.PriceByDate

interface InterCitiesRepository {

    suspend fun getRoutesInterCities(companyId: Int): List<RouteIntercityResponse>

    suspend fun getStopBusInterCities(routeId: Int): List<StopBusResponse>

    suspend fun getPriceAndHours(
        departureId: Int,
        arrivalId: Int,
        date: String,
        hour: String
    ): List<PriceByDate>

    suspend fun getHoursIntercities(
        departureId: Int,
        arrivalId: Int,
        date: String
    ): List<HoursResponse>
}