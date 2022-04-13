package com.smartgeeks.busticket.repository.price

import com.smartgeeks.busticket.domain.models.PriceByDate

interface PriceRepository {
    suspend fun getPriceByDate(
        departureId: Int,
        arrivalId: Int,
        passengerType: Int,
        date: String
    ): List<PriceByDate>
}