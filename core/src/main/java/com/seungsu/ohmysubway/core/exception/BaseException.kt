package com.seungsu.ohmysubway.core.exception

import java.io.IOException

class BaseException(
    val code: Int,
    override val message: String
) : IOException(message)

fun Throwable.asBaseExceptionOrNull(): BaseException? = this as? BaseException
