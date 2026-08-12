package com.seungsu.ohmysubway.core.mvi

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface Container<INTENT : ViewIntent, STATE : ViewState, EFFECT : ViewEffect> {
    val intent: SharedFlow<INTENT>
    val state: StateFlow<STATE>
    val effect: SharedFlow<EFFECT>
    val toastEffect: SharedFlow<String>
    val loadingEffect: SharedFlow<Boolean>

    suspend fun dispatchIntent(intent: INTENT)
    fun setState(reduce: STATE.() -> STATE)
    suspend fun sendEffect(effect: EFFECT)
    suspend fun sendToastEffect(message: String)
    suspend fun sendLoadingEffect(isLoading: Boolean)
}
