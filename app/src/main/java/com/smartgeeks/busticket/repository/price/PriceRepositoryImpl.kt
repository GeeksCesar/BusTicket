package com.smartgeeks.busticket.repository.price

import com.smartgeeks.busticket.data.api.PriceApi
import com.smartgeeks.busticket.domain.models.PriceByDate
import javax.inject.Inject

class PriceRepositoryImpl @Inject constructor(
    private val priceApi: PriceApi
) : PriceRepository {

    override suspend fun getPriceByDate(
        departureId: Int,
        arrivalId: Int,
        passengerType: Int,
        date: String
    ): List<PriceByDate> {
        return priceApi.getPricesByDate(departureId, arrivalId, passengerType, date).map {
            it.toDomain()
        }
    }
}