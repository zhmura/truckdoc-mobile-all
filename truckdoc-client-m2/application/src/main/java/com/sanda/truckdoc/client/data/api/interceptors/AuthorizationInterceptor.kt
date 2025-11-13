package com.sanda.truckdoc.client.data.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${getAuthToken()}")
            .build()
        return chain.proceed(request)
    }

    private fun getAuthToken(): String {
        // TODO: Implement token retrieval from secure storage
        return ""
    }
} 