package com.seungsu.ohmysubway.core.mvi

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class RealContainer<INTENT : ViewIntent, STATE : ViewState, EFFECT : ViewEffect>(
    initialState: STATE
) : Container<INTENT, STATE, EFFECT> {
    private val _intent = MutableSharedFlow<INTENT>()
    override val intent: SharedFlow<INTENT> = _intent.asSharedFlow()
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<STATE> = _state.asStateFlow()
    private val _effect = MutableSharedFlow<EFFECT>()
    override val effect: SharedFlow<EFFECT> = _effect.asSharedFlow()
    private val _toastEffect = MutableSharedFlow<String>()
    override val toastEffect: SharedFlow<String> = _toastEffect.asSharedFlow()
    private val _loadingEffect = MutableSharedFlow<Boolean>()
    override val loadingEffect: SharedFlow<Boolean> = _loadingEffect.asSharedFlow()

    override suspend fun dispatchIntent(intent: INTENT) { _intent.emit(intent) }
    override fun setState(reduce: STATE.() -> STATE) { _state.value = _state.value.reduce() }
    override suspend fun sendEffect(effect: EFFECT) { _effect.emit(effect) }
    override suspend fun sendToastEffect(message: String) { _toastEffect.emit(message) }
    override suspend fun sendLoadingEffect(isLoading: Boolean) { _loadingEffect.emit(isLoading) }
}
