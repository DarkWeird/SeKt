package me.darkweird.sekt

// TODO prettify
class WebDriverException(error: WebDriverResult.Error<*>) : RuntimeException("${error.error} : ${error.message}")

sealed class WebDriverResult<T> {
    data class Success<T>(val value: T) : WebDriverResult<T>()
    data class Error<T>(val error: String, val message: String, val stacktrace: String, val data: Any?) :
        WebDriverResult<T>()


    fun getOrNull(): T? =
        when (this) {
            is Success -> value
            is Error -> null
        }


    fun errorOrNull(): Error<T>? =
        when (this) {
            is Error -> this
            else -> null
        }

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    fun orThrow(): T =
        when (this) {
            is Success -> value
            is Error -> throw WebDriverException(this)
        }

    @Suppress("UNCHECKED_CAST") // Error don't matter about <T>
    fun <R> map(fn: T.() -> R): WebDriverResult<R> =
        when (this) {
            is Success -> Success(fn(value))
            is Error -> this as WebDriverResult<R>
        }

    fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Error<T>) -> R
    ): R =
        when (this) {
            is Success -> onSuccess(value)
            is Error -> onError(this)
        }

}
