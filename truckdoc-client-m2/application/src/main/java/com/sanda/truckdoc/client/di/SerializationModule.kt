package com.sanda.truckdoc.client.di

import com.sanda.truckdoc.client.util.json.IJsonParser
import com.sanda.truckdoc.client.util.json.JacksonParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SerializationModule {

    @Binds
    @Singleton
    abstract fun bindJsonParser(
        jacksonParser: JacksonParser
    ): IJsonParser
}

