package com.sanda.truckdoc.client.util.async

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.rx3.await

/**
 * Async Bridge: Extensions to bridge RxJava legacy code to Kotlin Coroutines.
 * Use these functions to consume RxJava sources as Coroutines/Flow.
 */

/**
 * Awaits for the Single to complete and returns the result.
 * Throws exception if Single errors.
 */
suspend fun <T : Any> Single<T>.toSuspend(): T = this.await()

/**
 * Awaits for the Completable to complete.
 * Throws exception if Completable errors.
 */
suspend fun Completable.toSuspend() = this.await()

/**
 * Converts an Observable to a Kotlin Flow.
 */
fun <T : Any> Observable<T>.toFlow(): Flow<T> = this.asFlow()

/**
 * Converts a Flowable to a Kotlin Flow.
 */
fun <T : Any> Flowable<T>.toFlow(): Flow<T> = this.asFlow()

