package com.seungsu.ohmysubway.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseViewModel : ViewModel() {
    private val errorHandler = CoroutineExceptionHandler { _, exception ->
        handleException(exception)
    }

    enum class SignalState { INITIALIZE, ERROR_REFRESH }

    private val refreshSignal = MutableSharedFlow<SignalState>()

    protected val loadDataSignal: Flow<SignalState> = flow {
        emit(SignalState.INITIALIZE)
        emitAll(refreshSignal)
    }

    open fun onRefresh(signalState: SignalState = SignalState.ERROR_REFRESH) = launch {
        refreshSignal.emit(signalState)
    }

    protected open fun handleException(exception: Throwable) {
        if (exception is CancellationException) return
        Timber.e(exception)
    }

    protected fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        errorHandler: CoroutineExceptionHandler = this.errorHandler,
        block: suspend CoroutineScope.() -> Unit
    ): Job = (viewModelScope + errorHandler).launch(context, start, block)
}
