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
    // get() 이 없으면 값이 **Gradle 데몬 JVM 에 한 번 계산된 채로 박힌다.** 데몬이 살아 있는
    // 동안에는 시각이 지나도 그대로여서, clean 도 --no-configuration-cache 도 소용이 없다.
    // 빌드는 BUILD SUCCESSFUL 로 끝나는데 APK 의 versionCode 는 옛날 값이라,
    // 배포해도 "올렸는데 같은 번호" 가 된다. 매번 다시 계산되도록 게터로 둔다.
    val versionName: String get() = getVersionNameByDate("yy.MM.dd")
    val versionCode: Int get() = getVersionNameByDate("yyMMddHH").toInt()

    private fun getVersionNameByDate(pattern: String): String {
        return ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern(pattern))
    }
}
