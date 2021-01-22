package com.words.storageapp.util

import java.lang.Exception

sealed class Status<out T> {
    class Success<out T>(val data: T) : Status<T>()
    class Error(val exception: Exception) : Status<Nothing>()
    object Loading : Status<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
            Loading -> "Loading"

        }
    }
}


sealed class State<T> {
    class Loading<T> : State<T>()
    data class Success<T>(val data: T) : State<T>()
    data class Failed<T>(val message: String) : State<T>()

    companion object {
        fun <T> loading() = Loading<T>()
        fun <T> success(data: T) = Success(data)
        fun <T> failed(message: String) = Failed<T>(message)
    }
}
