package com.seungsu.ohmysubway.core.mvi

import com.seungsu.ohmysubway.core.base.BaseViewModel
import com.seungsu.ohmysubway.core.ext.container
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

abstract class MVIViewModel<I : ViewIntent, S : ViewState, E : ViewEffect> :
    BaseViewModel(), ContainerHost<I, S, E> {

    abstract fun createInitialState(): S

    private val initialState: S by lazy { createInitialState() }

    override val container: Container<I, S, E> = container(initialState)

    val intent: SharedFlow<I> get() = container.intent
    val state: StateFlow<S> get() = container.state
    val effect: SharedFlow<E> get() = container.effect
    val toastEffect: SharedFlow<String> get() = container.toastEffect
    val loadingEffect: SharedFlow<Boolean> get() = container.loadingEffect

    protected inline fun <reified T> currentState(action: S.() -> T): T = state.value.action()

    protected inline fun <reified S : ViewState> currentStateIf(action: S.() -> Unit) {
        val currentState = state.value
        if (currentState is S) {
            currentState.action()
        }
    }

    init { subscribeAction() }

    private fun subscribeAction() {
        launch {
            intent.collect {
                try { processIntent(it) } catch (e: Throwable) { handleException(e) }
            }
        }
    }

    abstract suspend fun processIntent(intent: I)

    fun dispatch(intent: I) { launch { container.dispatchIntent(intent) } }

    protected fun setState(reduce: S.() -> S) { container.setState(reduce) }
    protected fun setEffect(builder: () -> E) { launch { container.sendEffect(builder()) } }
    protected fun setToastEffect(builder: () -> String) { launch { container.sendToastEffect(builder()) } }
    protected fun setLoadingEffect(builder: () -> Boolean) { launch { container.sendLoadingEffect(builder()) } }
}
