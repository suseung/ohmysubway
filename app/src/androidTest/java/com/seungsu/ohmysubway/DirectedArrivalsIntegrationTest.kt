package com.seungsu.ohmysubway

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.seungsu.ohmysubway.data.repository.SubwayLineRepositoryImpl
import com.seungsu.ohmysubway.domain.model.DirectedArrivals
import com.seungsu.ohmysubway.domain.usecase.GetDirectedArrivalsUseCase
import com.seungsu.ohmysubway.domain.usecase.SearchStationsUseCase
import com.seungsu.ohmysubway.widget.ArrivalWidgetUpdater
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 역 검색 + 실 API 도착정보 조회 통합 검증 (네트워크 필요). */
@RunWith(AndroidJUnit4::class)
class DirectedArrivalsIntegrationTest {

    private lateinit var searchStations: SearchStationsUseCase
    private lateinit var getDirectedArrivals: GetDirectedArrivalsUseCase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // 역 목록은 assets만 읽으므로 직접 조립한다
        val lineRepository = SubwayLineRepositoryImpl(context, Json { ignoreUnknownKeys = true })
        searchStations = SearchStationsUseCase(lineRepository, Dispatchers.IO)

        // 도착정보 조회는 API 키/네트워크 설정이 붙은 실제 Hilt 그래프에서 가져온다
        getDirectedArrivals = EntryPointAccessors
            .fromApplication(context, ArrivalWidgetUpdater.WidgetEntryPoint::class.java)
            .getDirectedArrivalsUseCase()
    }

    @Test
    fun 역명_검색이_노선정보와_함께_동작한다() = runBlocking {
        val gangnam = searchStations("강남")
        assertTrue("강남 검색 결과가 있어야 한다", gangnam.any { it.name == "강남" })
        assertEquals(
            listOf("2호선", "신분당선"),
            gangnam.first { it.name == "강남" }.lineNames.sorted(),
        )

        // API 역명은 "서울"이지만 사용자는 "서울역"으로 검색한다
        val seoul = searchStations("서울역")
        assertTrue("서울역 폴백 검색이 동작해야 한다", seoul.any { it.name == "서울" })

        // 병기역명도 부분 검색으로 찾을 수 있어야 한다
        assertTrue(searchStations("총신대입구").any { it.name == "총신대입구(이수)" })
    }

    @Test
    fun 반대방향_조회는_서로_다른_열차집합을_돌려준다() = runBlocking {
        val toSeongsu = getDirectedArrivals(GetDirectedArrivalsUseCase.Params("강남", "성수"))
        val toSadang = getDirectedArrivals(GetDirectedArrivalsUseCase.Params("강남", "사당"))

        assertTrue(toSeongsu is DirectedArrivals.Success)
        assertTrue(toSadang is DirectedArrivals.Success)

        val outer = (toSeongsu as DirectedArrivals.Success).arrivals
        val inner = (toSadang as DirectedArrivals.Success).arrivals

        // 2호선 강남역에서 성수 방면은 외선, 사당 방면은 내선 — 겹치는 열차가 없어야 한다
        outer.forEach { assertEquals("2호선", it.lineName) }
        inner.forEach { assertEquals("2호선", it.lineName) }

        val outerTrains = outer.map { it.arrival.trainLineName }.toSet()
        val innerTrains = inner.map { it.arrival.trainLineName }.toSet()
        assertTrue(
            "양방향 열차가 섞이면 방향 판별이 잘못된 것: $outerTrains vs $innerTrains",
            outerTrains.intersect(innerTrains).isEmpty(),
        )
    }

    @Test
    fun 같은노선이_아니면_NotConnected를_돌려준다() = runBlocking {
        val result = getDirectedArrivals(GetDirectedArrivalsUseCase.Params("강남", "불암산"))
        assertTrue(result is DirectedArrivals.NotConnected)
    }
}
