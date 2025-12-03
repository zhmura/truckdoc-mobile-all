package com.sanda.truckdoc.client

import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.service.NotificationHelper
import com.sanda.truckdoc.network.AuthorizedBackend
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltEntryPoint {
    fun messagesDatabaseService(): MessagesDatabaseService
    fun prefs(): Prefs
    fun notificationHelper(): NotificationHelper
    fun authorizedBackend(): AuthorizedBackend?
    fun backend(): com.sanda.truckdoc.network.Backend
} 