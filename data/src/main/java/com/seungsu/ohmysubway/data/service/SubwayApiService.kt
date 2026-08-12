package com.seungsu.ohmysubway.data.service

import com.seungsu.ohmysubway.data.model.RealtimeArrivalResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface SubwayApiService {

    @GET("api/subway/{apiKey}/json/realtimeStationArrival/0/{count}/{stationName}")
    suspend fun getRealtimeArrivals(
        @Path("apiKey") apiKey: String,
        @Path("count") count: Int,
        @Path("stationName") stationName: String,
    ): RealtimeArrivalResponse
}
