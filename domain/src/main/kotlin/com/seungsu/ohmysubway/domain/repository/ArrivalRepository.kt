package com.seungsu.ohmysubway.domain.repository

import com.seungsu.ohmysubway.domain.model.Arrival

interface ArrivalRepository {
    suspend fun getArrivals(stationName: String): List<Arrival>
}
