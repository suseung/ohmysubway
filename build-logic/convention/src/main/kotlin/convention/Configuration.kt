package convention

import org.gradle.api.JavaVersion
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Configuration {
    const val applicationId = "com.seungsu.ohmysubway"
    const val minSdk = 26
    const val targetSdk = 36
    const val compileSdk = 36

    val javaCompileTarget = JavaVersion.VERSION_17
    val versionName = getVersionNameByDate("yy.MM.dd")
    val versionCode = getVersionNameByDate("yyMMddHH").toInt()

    private fun getVersionNameByDate(pattern: String): String {
        return ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern(pattern))
    }
}
