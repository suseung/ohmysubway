package com.seungsu.ohmysubway.domain.usecase

import com.seungsu.ohmysubway.domain.di.IoDispatcher
import com.seungsu.ohmysubway.domain.model.AppVersion
import com.seungsu.ohmysubway.domain.model.LatestBuild
import com.seungsu.ohmysubway.domain.repository.AppUpdateRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/** 확인 결과. 서버에 닿지 않은 것과 "최신"을 섞지 않으려고 따로 둔다. */
sealed interface UpdateCheck {
    data object UpToDate : UpdateCheck
    data class Available(val latest: LatestBuild) : UpdateCheck
    data object Unavailable : UpdateCheck
}

class GetInstalledVersionUseCase @Inject constructor(
    private val repository: AppUpdateRepository,
) {
    operator fun invoke(): AppVersion = repository.installed()
}

class CheckAppUpdateUseCase @Inject constructor(
    private val repository: AppUpdateRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher,
) : UseCase<Unit, UpdateCheck>(dispatcher) {

    override suspend fun execute(params: Unit): UpdateCheck = try {
        val latest = repository.fetchLatest()
        if (latest.version.code > repository.installed().code) {
            UpdateCheck.Available(latest)
        } else {
            UpdateCheck.UpToDate
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        UpdateCheck.Unavailable
    }
}
