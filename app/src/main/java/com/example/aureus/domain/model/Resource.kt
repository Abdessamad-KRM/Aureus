package com.example.aureus.domain.model

/**
 * Generic resource wrapper for data states
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
    object Idle : Resource<Nothing>()
}

suspend fun <T> Resource<T>.onSuccess(action: suspend (T) -> Unit): Resource<T> {
    if (this is Resource.Success) {
        action(data)
    }
    return this
}

suspend fun <T> Resource<T>.onError(action: suspend (String, Throwable?) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        action(message, exception)
    }
    return this
}

suspend fun <T> Resource<T>.onLoading(action: suspend () -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        action()
    }
    return this
}