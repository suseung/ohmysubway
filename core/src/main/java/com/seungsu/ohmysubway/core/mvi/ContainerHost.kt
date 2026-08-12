package com.seungsu.ohmysubway.core.mvi

interface ContainerHost<I : ViewIntent, S : ViewState, E : ViewEffect> {
    val container: Container<I, S, E>
}
