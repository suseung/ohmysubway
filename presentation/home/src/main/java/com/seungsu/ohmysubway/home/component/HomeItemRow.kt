package com.seungsu.ohmysubway.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.seungsu.ohmysubway.design.compose.ThemePreview
import com.seungsu.ohmysubway.design.compose.theme.Blue50
import com.seungsu.ohmysubway.design.compose.theme.Grey05
import com.seungsu.ohmysubway.design.compose.theme.Grey60
import com.seungsu.ohmysubway.design.compose.theme.OhMySubwayTheme
import com.seungsu.ohmysubway.domain.model.Arrival
import com.seungsu.ohmysubway.domain.model.DirectedArrival
import com.seungsu.ohmysubway.domain.util.stationDisplayName

@Composable
fun HomeItemRow(
    directedArrival: DirectedArrival,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Grey05)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = directedArrival.lineName,
            style = OhMySubwayTheme.typos.bold.font12,
            color = Blue50,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = directedArrival.arrival.arrivalMessage,
                style = OhMySubwayTheme.typos.bold.font16,
            )
            Text(
                text = "${directedArrival.arrival.terminalStation.stationDisplayName}행 · ${directedArrival.arrival.trainStatus}",
                style = OhMySubwayTheme.typos.regular.font12,
                color = Grey60,
            )
        }
    }
}

@ThemePreview
@Composable
private fun HomeItemRowPreview() {
    OhMySubwayTheme {
        HomeItemRow(
            directedArrival = DirectedArrival(
                lineName = "2호선",
                arrival = Arrival(
                    subwayId = "1002",
                    stationName = "강남",
                    updnLine = "외선",
                    trainLineName = "성수행 - 역삼방면",
                    terminalStation = "성수",
                    secondsToArrival = 180,
                    arrivalMessage = "3분 후 (교대)",
                    arrivalCode = "99",
                    trainStatus = "일반",
                    receivedAt = "2026-08-12 10:00:00",
                ),
            ),
        )
    }
}
