package com.seungsu.ohmysubway.domain.model

/** 설치된 앱 또는 배포 서버의 최신 빌드. [code] 가 클수록 새 버전이다. */
data class AppVersion(
    val code: Long,
    val name: String,
)

/**
 * 배포 서버에 올라온 최신 빌드. [pageUrl] 은 서버의 앱 페이지(빌드 목록) 주소다 —
 * 로그인이 안 돼 있으면 로그인 화면이 먼저 뜨고, 로그인하면 이 페이지로 돌아온다.
 */
data class LatestBuild(
    val version: AppVersion,
    val note: String,
    val pageUrl: String,
)
