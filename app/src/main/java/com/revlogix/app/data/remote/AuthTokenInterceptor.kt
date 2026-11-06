package com.revlogix.app.data.remote

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.revlogix.app.auth.FirebaseSession
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adds the signed-in user's Firebase ID token to every API request.
 * The API validates the token, so the user's data is only reachable by that user.
 * OkHttp runs interceptors on a background thread, so blocking on the token task is fine here.
 */
class AuthTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = try {
            FirebaseSession.user()?.let { Tasks.await(it.getIdToken(false)).token }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get ID token: ${e.message}")
            null
        }

        val request = chain.request().newBuilder().apply {
            if (token != null) header("Authorization", "Bearer $token")
        }.build()
        return chain.proceed(request)
    }

    private companion object {
        const val TAG = "AuthTokenInterceptor"
    }
}
