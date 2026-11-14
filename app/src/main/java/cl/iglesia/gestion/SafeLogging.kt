package cl.iglesia.gestion

import android.util.Log

/** Logs the failure category without exception messages, headers, or request data. */
internal fun logFailure(tag: String, context: String, error: Throwable) {
    val type = error::class.simpleName ?: "Exception"
    Log.w(tag, "$context ($type)")
}
