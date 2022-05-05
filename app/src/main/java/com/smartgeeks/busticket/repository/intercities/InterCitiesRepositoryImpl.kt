package com.smartgeeks.busticket.repository.intercities

import com.smartgeeks.busticket.data.api.InterCitiesApi
import com.smartgeeks.busticket.data.models.intercities.HoursResponse
import com.smartgeeks.busticket.data.models.intercities.RoutesIntercityResponse
import com.smartgeeks.busticket.data.models.intercities.StopBusResponse
import com.smartgeeks.busticket.domain.models.PriceByDate
import javax.inject.Inject

class InterCitiesRepositoryImpl @Inject constructor(
    private val interCitiesApi: InterCitiesApi
) : InterCitiesRepository {
    override suspend fun getRoutesInterCities(companyId: Int): List<RoutesIntercityResponse> {
        return interCitiesApi.getRoutesInterCities(companyId)
    }

    override suspend fun getStopBusInterCities(routeId: Int): List<StopBusResponse> {
        return interCitiesApi.getStopBusInterCities(routeId)
    }

    override suspend fun getPriceAndHours(
        departureId: Int,
        arrivalId: Int,
        date: String,
        hour : String
    ): List<PriceByDate> {
        return interCitiesApi.getPriceAndHours(departureId, arrivalId, date, hour).map { it.toDomain() }
    }

    override suspend fun getHoursIntercities(
        departureId: Int,
        arrivalId: Int,
        date: String
    ): List<HoursResponse> {
        return interCitiesApi.getHoursIntercities(departureId, arrivalId, date)
    }
}