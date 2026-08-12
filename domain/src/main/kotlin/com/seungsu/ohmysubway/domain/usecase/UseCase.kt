package com.seungsu.ohmysubway.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

abstract class UseCase<P, R>(private val dispatcher: CoroutineDispatcher) {
    protected abstract suspend fun execute(params: P): R
    suspend operator fun invoke(params: P): R = withContext(dispatcher) { execute(params) }
}

abstract class FlowUseCase<P, R>(private val dispatcher: CoroutineDispatcher) {
    abstract fun execute(params: P): Flow<R>
    operator fun invoke(params: P): Flow<R> = execute(params).flowOn(dispatcher)
}
