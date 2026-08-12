package com.seungsu.ohmysubway.core.ext

import com.seungsu.ohmysubway.core.mvi.Container
import com.seungsu.ohmysubway.core.mvi.RealContainer
import com.seungsu.ohmysubway.core.mvi.ViewEffect
import com.seungsu.ohmysubway.core.mvi.ViewIntent
import com.seungsu.ohmysubway.core.mvi.ViewState

fun <I : ViewIntent, S : ViewState, E : ViewEffect> container(initialState: S): Container<I, S, E> =
    RealContainer(initialState)
