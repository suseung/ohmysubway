package com.seungsu.ohmysubway.domain.repository

import com.seungsu.ohmysubway.domain.model.AppVersion
import com.seungsu.ohmysubway.domain.model.LatestBuild

interface AppUpdateRepository {
    fun installed(): AppVersion

    /** 서버가 꺼져 있거나 닿지 않으면 예외를 던진다. */
    suspend fun fetchLatest(): LatestBuild
}
