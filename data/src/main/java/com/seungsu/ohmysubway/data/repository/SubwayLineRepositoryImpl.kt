package com.seungsu.ohmysubway.data.repository

import android.content.Context
import com.seungsu.ohmysubway.data.model.SubwayLinesDto
import com.seungsu.ohmysubway.domain.model.SubwayLine
import com.seungsu.ohmysubway.domain.model.SubwayRoute
import com.seungsu.ohmysubway.domain.repository.SubwayLineRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubwayLineRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) : SubwayLineRepository {

    private val mutex = Mutex()
    private var cached: List<SubwayLine>? = null

    override suspend fun getLines(): List<SubwayLine> = mutex.withLock {
        cached ?: loadFromAssets().also { cached = it }
    }

    private fun loadFromAssets(): List<SubwayLine> {
        val raw = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        return json.decodeFromString<SubwayLinesDto>(raw).lines.map { line ->
            SubwayLine(
                subwayId = line.subwayId,
                name = line.name,
                routes = line.routes.map { route ->
                    SubwayRoute(
                        name = route.name,
                        circular = route.circular,
                        stations = route.stations,
                    )
                },
            )
        }
    }

    companion object {
        private const val ASSET_PATH = "subway_lines.json"
    }
}
