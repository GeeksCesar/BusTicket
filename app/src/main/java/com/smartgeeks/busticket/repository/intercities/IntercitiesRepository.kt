package com.smartgeeks.busticket.repository.intercities

import com.smartgeeks.busticket.data.models.intercities.RoutesIntercityResponse
import com.smartgeeks.busticket.data.models.intercities.StopBusResponse
import com.smartgeeks.busticket.domain.models.PriceByDate

interface InterCitiesRepository {

    suspend fun getRoutesInterCities(companyId: Int): List<RoutesIntercityResponse>

    suspend fun getStopBusInterCities(routeId: Int): List<StopBusResponse>

    suspend fun getPriceAndHours(
        departureId: Int,
        arrivalId: Int,
        date: String
    ): List<PriceByDate>
}