# Android 위젯 정리 (Glance 기준)

이 프로젝트의 홈 화면 위젯을 만들면서 정리한 내용. 개념 → 파일 구조 → 동작 흐름 →
실제로 부딪힌 함정 순으로 적었다. 코드 경로는 모두 `presentation/widget/` 기준이다.

---

## 1. 근본 전제: 위젯은 내 앱에서 그려지지 않는다

위젯 뷰는 **런처 프로세스**에 살고, 우리 앱은 "이렇게 그려줘"라는 **설계도(RemoteViews)** 만
만들어 보낸다. 여기서 모든 제약이 파생된다.

- 아무 View나 못 쓴다 — `TextView`, `ImageView`, `LinearLayout`, `Chronometer`, `ViewFlipper` 등 허용 목록만
- 클릭 리스너에 람다를 넘길 수 없다 (다른 프로세스이므로) → `PendingIntent` 기반
- 1초마다 갱신하면 매번 프로세스 간 통신이라 비싸다
- 앱이 실행 중이 아니어도 위젯은 화면에 남아 있다

**Glance** 는 이 RemoteViews를 Compose 문법으로 감싸주는 라이브러리다. 결국 만들어내는 건
같은 RemoteViews이고, 제약도 그대로 물려받는다.

---

## 2. 구성 요소와 파일 지도

| 파일 | 역할 |
|---|---|
| `AndroidManifest.xml` | 시스템에 위젯 존재를 신고 (receiver + 메타데이터) |
| `res/xml/arrival_widget_info.xml` | 위젯 명세서 — 크기, 갱신주기, 설정화면, 미리보기 |
| `ArrivalWidgetReceiver.kt` | 시스템 ↔ 위젯 연결 대문 (한 줄) |
| `ArrivalAppWidget.kt` | **화면 그리기** |
| `ArrivalWidgetData.kt` | **저장할 데이터 모양** + 판단 함수 |
| `ArrivalWidgetUpdater.kt` | **핵심 로직** — 조회·저장·갱신·알람 예약 |
| `RefreshArrivalAction.kt` | 탭 이벤트 수신 |
| `ArrivalWidgetRerenderReceiver.kt` | 알람 수신 → 다시 그리기 |
| `WidgetColors.kt` | 배경색 밝기 → 글자색 자동 결정 |
| `config/` 4개 | 위젯 추가/재설정 화면 (MVI) |
| `res/layout/widget_countdown.xml` | 초를 세는 `Chronometer` |
| `res/layout/arrival_widget_preview.xml` | 위젯 목록 미리보기 |

기억할 건 셋뿐이다. **`ArrivalAppWidget`(그리기) · `ArrivalWidgetUpdater`(로직) ·
`ArrivalWidgetData`(값)**. 나머지는 안드로이드가 요구하는 연결 부품이거나 설정 화면이다.

---

## 3. 동작 흐름

입구가 셋, 두뇌는 하나, 데이터는 저장소를 경유한다.

```
[입구]                          [두뇌]              [저장소]           [그리기]

탭 → RefreshArrivalAction ─┐
                            ├→ ArrivalWidgetUpdater → DataStore → ArrivalAppWidget
알람 → RerenderReceiver ───┘                            ▲              │
                                                        └──────────────┘
시스템(추가·재부팅·크기변경) → ArrivalWidgetReceiver ──────────→ ArrivalAppWidget
```

핵심은 두 가지다.

**Updater와 ArrivalAppWidget은 서로를 호출하지 않는다.** Updater는 저장소에 쓰고
"다시 그려줘"라는 신호만 보내고(`update()`), ArrivalAppWidget은 저장소에서 직접 읽는다.

```kotlin
// ArrivalAppWidget — 저장소에서 스스로 읽는다
val prefs = currentState<Preferences>()
val data = ArrivalWidgetData.decode(prefs[ArrivalWidgetData.PREF_KEY])
```

**시스템이 그릴 때는 Updater가 개입하지 않는다.** 재부팅·크기변경·다크모드 전환 등으로
`provideGlance`가 다시 불리면 저장된 값으로 바로 그린다. 그래서 렌더 코드는 항상
"그리는 순간"을 기준으로 계산해야 한다 (미리 계산해 저장한 값을 쓰면 시간이 지나 어긋난다).

---

## 4. 상태 저장

위젯은 **인스턴스마다 독립된 상태**가 필요하다 (강남→성수 위젯과 사당→서울 위젯이 따로 존재).
Glance는 `GlanceId` 별 DataStore를 제공한다. 우리는 데이터 클래스를 JSON으로 직렬화해 한 칸에 넣었다.

```kotlin
@Serializable
data class ArrivalWidgetData(
    val startStation: String,        // "강남"
    val destinationStation: String,  // "성수"
    val updatedAtMillis: Long,       // 조회 시각 → 30초 캐시 판단
    val loading: Boolean,            // 조회 중 → 중복 호출 방지
    val arrivals: List<WidgetArrivalItem>,
    val appearance: WidgetAppearance,
)
```

판단 로직을 데이터 옆에 둬서 Updater가 물어보게 했다.

```kotlin
fun isFresh(now: Long)      = updatedAtMillis > 0 && now - updatedAtMillis < 30_000
fun isRefreshing(now: Long) = loading && now - loadingStartedAtMillis < 20_000
```

`isRefreshing` 에 시간 제한이 있는 이유: 조회 중 프로세스가 죽으면 `loading` 이 `true` 로
남아 이후 모든 새로고침이 무시된다. 오래된 `loading` 은 진행 중이 아닌 것으로 본다.

### `Locked` 접미어 규약

```kotlin
private val stateMutex = Mutex()

suspend fun readData(...) = stateMutex.withLock { readDataLocked(...) }  // 잠그고 호출
private suspend fun readDataLocked(...)                                  // 잠긴 상태 가정
```

**코틀린 `Mutex` 는 재진입이 안 된다.** `withLock` 안에서 또 `withLock` 하면 영구 대기(데드락)다.
그래서 "잠그는 함수"와 "잠긴 상태를 가정하는 함수"를 분리했다. `configure()` 처럼 이미 잠금을
잡은 곳에서 `writeDataLocked` 를 부르기 때문에 필요한 구조다.

### `writeDataLocked` 가 세 가지를 하는 이유

```kotlin
updateAppWidgetState(...) { prefs[PREF_KEY] = data.encode() }  // ① 저장
ArrivalAppWidget().update(context, glanceId)                   // ② 다시 그리라고 알림
scheduleNextRerender(context, listOf(glanceId))                // ③ 정지 시점 알람 예약
```

**Glance는 상태를 저장해도 자동으로 다시 그리지 않는다.** ②를 빼면 데이터는 바뀌었는데
화면은 옛것 그대로다.

---

## 5. 상호작용

```kotlin
// 붙이는 쪽
GlanceModifier.clickable(actionRunCallback<RefreshArrivalAction>())

// 실행되는 쪽
class RefreshArrivalAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        ArrivalWidgetUpdater.refresh(context, glanceId)
    }
}
```

- **`suspend`** 라서 네트워크 호출을 여기서 바로 해도 된다
- 앱이 꺼져 있으면 **탭 시점에 프로세스가 뜬다**
- **`glanceId`** 로 "어느 위젯이 눌렸는지" 구분한다 — 위젯이 여러 개일 때 필수
- 매니페스트 등록이 필요 없다 (Glance가 연결)

다른 액션: `actionStartActivity`(앱 열기), `actionSendBroadcast`, `actionStartService`.

---

## 6. 갱신 전략

이 프로젝트는 **탭할 때만 조회**한다. 자동 갱신을 넣지 않은 이유와 대안:

| 방법 | 한계 |
|---|---|
| `updatePeriodMillis` | **최소 30분**. 그보다 촘촘하게는 불가능 |
| `AlarmManager.set()` (부정확) | 권한 불필요하지만 안드로이드가 미룬다. 앱을 자주 안 쓰면 더 밀림 |
| `setExactAndAllowWhileIdle` | 정확하지만 `SCHEDULE_EXACT_ALARM` 권한 필요 (Android 12+) |
| `WorkManager` 주기 작업 | **최소 15분** |
| 1초마다 직접 갱신 | IPC 비용 + 스로틀링 대상. 하지 말 것 |

### 30초 캐시

우리가 쓰는 API가 약 24초(18~30초) 주기로만 갱신되므로, 그 안에 다시 조회해도 같은 데이터가 온다.
그래서 탭해도 30초 안이면 네트워크를 생략하고 화면만 다시 그린다. 호출 수(일 1,000회 제한)를
아끼고 반응도 즉각적이다.

---

## 7. Chronometer로 초를 흐르게 하기

1초마다 위젯을 갱신하는 건 비싸다. 대신 `Chronometer` 를 넣고 **기준 시각만 넘기면 런처가 대신 센다.**
우리 앱은 아무것도 하지 않고, 화면을 안 보면 자동으로 멈춘다.

```kotlin
val remoteViews = RemoteViews(context.packageName, R.layout.widget_countdown).apply {
    setChronometer(R.id.widget_countdown, SystemClock.elapsedRealtime() + (arrivalAt - now), "%s 후", true)
    setChronometerCountDown(R.id.widget_countdown, true)
    setTextColor(R.id.widget_countdown, colors.primaryText.toArgb())
}
AndroidRemoteViews(remoteViews = remoteViews, modifier = modifier)
```

Glance에 없는 뷰가 필요할 때 `AndroidRemoteViews` 로 부분 탈출하는 예이기도 하다.

### 왜 `SystemClock.elapsedRealtime()` 인가

| API | 기준 | 특징 |
|---|---|---|
| `System.currentTimeMillis()` | 1970-01-01 | 실제 시각. 시간 변경·NTP 동기화 시 **점프** |
| `SystemClock.elapsedRealtime()` | 부팅 시점 | 단조 증가, 절전 중에도 흐름 |
| `SystemClock.uptimeMillis()` | 부팅 시점 | 단조 증가, **절전 중 멈춤** |

`Chronometer` 는 부팅경과 시간대로만 동작한다. 우리는 도착 시각을 **실제 시각(epoch)으로 저장**하고,
**그릴 때만** 부팅경과 시간대로 변환한다. 부팅경과 시간은 재부팅하면 0이 되므로 저장에 쓸 수 없다.

`currentTimeMillis` 를 그대로 넘기면 값이 1조를 넘어 "수십 년 남음"으로 표시된다.

### 카운트다운의 원리와 함정

`setChronometerCountDown(true)` 를 켜면 계산식이 뒤집힌다.

```
기본:      표시값 = 지금 - base    (base 가 과거 → 증가)
카운트다운: 표시값 = base - 지금    (base 가 미래 → 감소)
```

시간은 항상 앞으로 흐르고, 고정된 미래 지점에서 현재를 빼기 때문에 숫자가 줄어든다.

**문제는 0에서 멈추지 않는다는 것이다.**

| 경과 | 표시 |
|---|---|
| 0초 | `02:30 후` |
| 150초 | `00:00` |
| 180초 | **`-00:30 후`** |

`Chronometer` 에는 0을 특별히 다루는 로직이 없다. 값으로는 절대 멈추지 않는다.

### 틱이 멈추는 조건은 "값"이 아니라 "보이는지"

```
틱 실행 = 시작됨(started) && 화면에 보임(visible)
```

- 다른 앱을 열거나 화면이 꺼지면 → 틱 중단
- 홈 화면으로 돌아오면 → **현재 시각으로 새로 계산해서** 재개

즉 틱 중단은 값을 보존하지 않는다. 오래 안 보다 돌아오면 그동안 흐른 만큼 반영돼
**더 큰 음수**가 뜬다. 그래서 알람 없이는 해결되지 않는다.

### 그래서 알람이 필요하다

`scheduleNextRerender` 가 **가장 이른 도착 시각 +1초**에 알람을 예약하고, 울리면
`ArrivalWidgetRerenderReceiver` → `rerenderAll()` 로 위젯을 다시 그린다. 그 시점엔
`now >= arrivalAt` 이므로 Chronometer 대신 **정적 텍스트 `0초`** 가 그려진다.
다시 그릴 때 그다음 도착 시각을 또 예약해 체인으로 이어진다.

```kotlin
alarmManager.set(AlarmManager.RTC, nextBoundary, pendingIntent)
```

- `RTC` (≠ `RTC_WAKEUP`) — 기기를 깨우지 않는다. 화면이 꺼져 있으면 아무도 안 보므로
- 고정 request code + `FLAG_UPDATE_CURRENT` — 예약이 쌓이지 않고 항상 "가장 이른 것 하나"만 존재
- 예약할 게 없으면 `cancel()`

**한계**: 부정확 알람이라 밀릴 수 있다. 밀리는 동안 홈 화면을 보고 있으면 음수가 보인다.
탭하면 즉시 정상화된다. 완전히 없애려면 `SCHEDULE_EXACT_ALARM` 권한이 필요하다.

### 선택지 정리

| 방식 | 결과 |
|---|---|
| 알람 없이 Chronometer만 | 볼 때마다 음수, 시간이 지날수록 커짐 |
| **부정확 알람 (현재)** | 대체로 0에서 멈춤. 밀리면 잠깐 음수 |
| 정확 알람 (권한 필요) | 항상 0에서 멈춤 |
| 정적 텍스트 | 음수 없음. 대신 초가 안 흐름 |

### 반드시 지킬 것: 표시값은 렌더 시점에 계산한다

"멈출 때의 값"을 미리 계산해 고정하면, 나중에 다시 그릴 때 **숫자가 거꾸로 올라간다**
(이미 0에 가까웠는데 `0:20` 이 다시 뜨는 현상). 항상 `arrivalAt - now` 로 그 순간 계산해야
단조 감소가 보장된다.

---

## 8. 크기 대응

```xml
android:minWidth="110dp"  android:minHeight="40dp"
android:targetCellWidth="2"  android:targetCellHeight="1"   <!-- API 31+ -->
android:resizeMode="horizontal|vertical"
```

```kotlin
override val sizeMode = SizeMode.Exact   // 실제 크기를 받는다

val size = LocalSize.current
val compact = size.height < 80.dp
val showTerminal = size.width >= 210.dp        // 좁으면 종착역 숨김
val maxRows = ((size.height.value - 30) / 20).toInt().coerceIn(1, 4)
```

`SizeMode` 세 가지: `Single`(하나로 고정), `Exact`(실제 크기마다 호출), `Responsive`(정의한 크기 집합).

---

## 9. 위젯 목록 미리보기

| 방식 | 조건 | 대상 |
|---|---|---|
| **XML `previewLayout`** | Android 12+ | 대부분의 기기 — 현재 사용 중 |
| 컴포저블 `providePreview` | Glance 1.2+ **그리고** Android 15+ | 최신 기기만 |
| Android Studio `@Preview` | `glance-appwidget-preview` 의존성 | 개발자용 |

`previewLayout` 은 시스템이 직접 인플레이트하므로 **Compose가 아니라 RemoteViews 호환 뷰**로
짜야 한다. 실제 데이터가 없으니 예시 문자열을 하드코딩한다.

컴포저블 미리보기(`providePreview`)는 Android 15 미만에서 동작하지 않아 XML을 병행 유지해야 한다.
미리보기 정의가 두 곳으로 늘어나므로, minSdk가 낮으면 XML 하나로 가는 게 관리가 쉽다.

---

## 10. 설정 액티비티

`android:configure` 를 지정하면 위젯 추가 시 이 액티비티가 먼저 뜬다.

**반드시 이렇게 끝내야 위젯이 실제로 붙는다.**

```kotlin
setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
finish()
```

기본값은 `setResult(RESULT_CANCELED)` 로 두어, 사용자가 뒤로 가면 위젯이 생기지 않게 한다.

**재설정도 같은 액티비티로 들어온다** (런처에서 위젯 길게 누르기). 이때 기존 설정을 불러와
채워주지 않으면 사용자가 처음부터 다시 입력해야 한다.

---

## 11. 실제로 부딪힌 함정

**Hilt 주입이 안 된다.** 위젯 콜백은 안드로이드가 생성하므로 `@Inject` 를 못 쓴다.
`EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)` 로 꺼낸다.

**DataStore 동시 접근이 크래시를 낸다.** Glance가 위젯을 그리는 중에 우리가 상태를 읽으면
`multiple DataStores active` 예외로 앱이 죽는다. 위젯을 빠르게 두 번 누르면 재현된다.
→ 상태 접근을 한 곳으로 모으고 **Mutex + 재시도**(예외 메시지로 판별해 100ms씩 늘려 5회).

**앱을 재설치하면 위젯 상태가 날아간다.** 홈에 위젯은 남지만 내용이 비어 빈 상자가 된다.
설정이 없을 때의 안내 문구를 반드시 그려주고, 탭하면 그 문구가 뜨도록 해둔다.

**`force-stop` 하면 위젯이 통째로 초기화된다.** 안드로이드 기본 동작이라 앱 버그가 아니다.
디버깅할 때 헷갈리니 알아둘 것.

**한 번에 위젯을 여럿 갱신하면 일부가 누락된다.** 테스트로 위젯이 15개 쌓였을 때 겪었다.
실사용에선 드물다.

**테스트에서 상태를 정상 경로 밖으로 쓰면 화면이 안 바뀐다.** Glance 내부 캐시와 어긋난다.
테스트도 앱이 쓰는 것과 같은 경로(`ArrivalWidgetUpdater`)를 타야 한다.

---

## 12. 테스트와 디버깅

```bash
# 계측 테스트에서 AppWidgetHost 로 위젯을 바인딩하려면 필요
adb shell appwidget grantbind --package com.seungsu.ohmysubway

# 현재 붙은 위젯 인스턴스, 호스트, 예약된 알람 확인
adb shell dumpsys appwidget
adb shell dumpsys alarm | grep -A3 <패키지명>

# 화면 전환/시작 화면의 순간을 잡아야 할 때 (애니메이션 5배 느리게)
adb shell settings put global window_animation_scale 5
adb shell settings put global transition_animation_scale 5

# 위젯 UI 노드 확인 (텍스트·bounds·selected 상태)
adb shell uiautomator dump /sdcard/ui.xml && adb shell cat /sdcard/ui.xml
```

계측 테스트에서 할 수 있는 것:

- `AppWidgetHost.allocateAppWidgetId()` + `bindAppWidgetIdIfAllowed()` 로 위젯 바인딩
- `GlanceAppWidgetManager.getGlanceIdBy(appWidgetId)` 로 `GlanceId` 획득
- `ArrivalWidgetUpdater.configure(...)` 호출 → 저장된 상태를 읽어 검증
- `requestPinGlanceAppWidget()` 으로 홈에 고정해 실제 렌더링을 스크린샷으로 확인

이 프로젝트의 `app/src/androidTest/` 에 위 방식으로 만든 테스트가 있다.

---

## 13. 참고

- 위젯 모듈: `presentation/widget/`
- 계측 테스트: `app/src/androidTest/java/com/seungsu/ohmysubway/`
- [Glance 공식 문서](https://developer.android.com/develop/ui/compose/glance)
- [앱 위젯 개요](https://developer.android.com/develop/ui/views/appwidgets/overview)
