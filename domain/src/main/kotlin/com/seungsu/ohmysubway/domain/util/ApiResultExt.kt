package com.seungsu.ohmysubway.domain.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

fun <T> Flow<T>.asResult(): Flow<ApiResult<T>> = this
    .map<T, ApiResult<T>> { ApiResult.Success(it) }
    .onStart { emit(ApiResult.Loading) }
    .catch { emit(ApiResult.Error(it)) }
