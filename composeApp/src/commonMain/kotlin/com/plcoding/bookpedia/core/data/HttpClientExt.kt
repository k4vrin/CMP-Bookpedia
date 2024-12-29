package com.plcoding.bookpedia.core.data

import co.touchlab.kermit.Logger
import com.plcoding.bookpedia.core.domain.DataError
import com.plcoding.bookpedia.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.serialization.JsonConvertException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import kotlin.coroutines.coroutineContext

suspend inline fun <reified T> safeCall(
    execute: () -> HttpResponse,
): Result<T, DataError.Remote> {
    val response = try {
        execute()
    } catch (e: SocketTimeoutException) {
        Logger.d("safeCall") { "SocketTimeoutException: $e" }
        return Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    } catch (e: UnresolvedAddressException) {
        Logger.d("safeCall") { "UnresolvedAddressException: $e" }
        return Result.Error(DataError.Remote.NO_INTERNET_CONNECTION)
    } catch (e: Exception) {
        Logger.d("safeCall") { "Exception: $e" }
        coroutineContext.ensureActive()
        return Result.Error(DataError.Remote.UNKNOWN)
    }
    return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(
    response: HttpResponse,
): Result<T, DataError.Remote> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Logger.d { "NoTransformationFoundException: $e" }
                Result.Error(DataError.Remote.SERIALIZATION_ERROR)
            } catch (e: SerializationException) {
                Logger.d { "SerializationException: $e" }
                Result.Error(DataError.Remote.SERIALIZATION_ERROR)
            } catch (e: JsonConvertException) {
                Logger.d { "JsonConvertException: $e" }
                Result.Error(DataError.Remote.SERIALIZATION_ERROR)
            }
        }

        408 -> Result.Error(DataError.Remote.REQUEST_TIMEOUT)
        409 -> Result.Error(DataError.Remote.TOO_MANY_REQUESTS)
        in 500..599 -> Result.Error(DataError.Remote.SERVER_ERROR)
        else -> Result.Error(DataError.Remote.UNKNOWN)
    }
}