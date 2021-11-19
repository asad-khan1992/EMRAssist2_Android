package com.emrassist.audio.retrofit

import java.lang.Exception

sealed class DataState<out R> {
    data class Success<out T>(val data: T) : DataState<T>()
    data class ErrorException(val exception: Throwable) : DataState<Nothing>()
    object Loading : DataState<Nothing>()
}