package com.revlogix.app.data.sync

/**
 * Decides what to do with a queued change after the API has answered.
 * Kept free of Android classes so it can be unit tested on the JVM.
 */
object SyncPolicy {
    const val MAX_RETRIES = 5

    enum class Outcome { DONE, RETRY, GIVE_UP }

    fun outcomeForHttp(code: Int): Outcome = when {
        code in 200..299 -> Outcome.DONE
        // Server busy / waking up, rate limited, or the token has not been accepted yet
        code == 401 || code == 408 || code == 429 || code >= 500 -> Outcome.RETRY
        // Anything else (400, 404 ...) will never work no matter how often we resend it
        else -> Outcome.GIVE_UP
    }

    /** True once an item has used up its attempts and should be marked FAILED. */
    fun hasExhaustedRetries(retryCountSoFar: Int): Boolean = retryCountSoFar + 1 >= MAX_RETRIES
}
