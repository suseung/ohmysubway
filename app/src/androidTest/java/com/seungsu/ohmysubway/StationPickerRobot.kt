package com.seungsu.ohmysubway

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import java.io.File

/** 역 검색 필드를 다루는 공용 헬퍼 — 입력 필드와 결과 행의 텍스트가 같아 구분이 필요하다. */
class StationPickerRobot(private val device: UiDevice) {

    fun editFields(): List<UiObject2> =
        device.wait(Until.findObjects(By.clazz(EDIT_TEXT_CLASS)), TIMEOUT_MS) ?: emptyList()

    /** 필드에 역명을 입력하고, 검색 결과 행(입력 필드가 아닌 쪽)을 눌러 선택한다. */
    fun selectStation(field: UiObject2, name: String) {
        field.text = name
        assertTrue(
            "입력한 '$name'이 화면에 보여야 한다",
            device.wait(Until.hasObject(By.text(name)), TIMEOUT_MS),
        )

        // 입력 필드와 결과 행의 텍스트가 같으므로, 필드가 아닌 노드가 나타날 때까지 기다린다
        val resultRow = awaitResultRow(name)
        assertTrue("'$name' 검색 결과 행이 있어야 한다", resultRow != null)
        resultRow!!.click()
        device.waitForIdle()
    }

    private fun awaitResultRow(name: String): UiObject2? {
        repeat(RESULT_POLL_ATTEMPTS) {
            device.findObjects(By.text(name))
                .firstOrNull { it.className != EDIT_TEXT_CLASS }
                ?.let { return it }
            device.waitForIdle(RESULT_POLL_INTERVAL_MS)
        }
        return null
    }

    fun screenshot(name: String) {
        val dir = InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir(null) ?: return
        device.takeScreenshot(File(dir, name))
    }

    companion object {
        const val EDIT_TEXT_CLASS = "android.widget.EditText"
        const val TIMEOUT_MS = 5_000L
        private const val RESULT_POLL_ATTEMPTS = 20
        private const val RESULT_POLL_INTERVAL_MS = 300L
    }
}
