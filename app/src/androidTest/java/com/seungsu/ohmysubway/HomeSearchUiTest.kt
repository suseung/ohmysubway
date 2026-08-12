package com.seungsu.ohmysubway

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** 홈 화면에서 역을 검색·선택하고 도착정보를 조회하는 UI 흐름 검증. */
@RunWith(AndroidJUnit4::class)
class HomeSearchUiTest {

    private lateinit var device: UiDevice
    private lateinit var robot: StationPickerRobot

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
        robot = StationPickerRobot(device)

        val context = instrumentation.targetContext
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        assertTrue(
            "앱 화면이 떠야 한다",
            device.wait(Until.hasObject(By.text("오마이지하철")), TIMEOUT_MS),
        )
    }

    @Test
    fun 역을_검색해_선택하고_도착정보를_조회한다() {
        val fields = robot.editFields()
        assertTrue("입력 필드가 2개 있어야 한다", fields.size >= 2)

        robot.selectStation(fields[0], "강남")
        robot.selectStation(fields[1], "성수")

        val lookupButton = device.wait(Until.findObject(By.text("도착정보 조회")), TIMEOUT_MS)
        assertNotNull("조회 버튼이 있어야 한다", lookupButton)
        assertTrue("두 역을 고르면 조회 버튼이 활성화돼야 한다", lookupButton.isEnabled)
        lookupButton.click()

        // 도착 메시지("N분 M초 후", "전역 출발" 등) 또는 없음 안내가 떠야 한다
        val shown = device.wait(Until.hasObject(By.textContains("후")), LOOKUP_TIMEOUT_MS) ||
            device.hasObject(By.textContains("도착")) ||
            device.hasObject(By.textContains("열차"))
        robot.screenshot("home_search.png")
        assertTrue("조회 결과가 표시돼야 한다", shown)
    }

    companion object {
        private const val TIMEOUT_MS = 5_000L
        private const val LOOKUP_TIMEOUT_MS = 15_000L
    }
}
