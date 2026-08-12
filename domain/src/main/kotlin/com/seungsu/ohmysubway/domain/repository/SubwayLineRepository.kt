package com.seungsu.ohmysubway.domain.repository

import com.seungsu.ohmysubway.domain.model.SubwayLine

interface SubwayLineRepository {
    suspend fun getLines(): List<SubwayLine>
}
