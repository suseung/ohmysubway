package com.seungsu.ohmysubway.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.seungsu.ohmysubway.domain.di.DefaultDispatcher
import com.seungsu.ohmysubway.domain.di.IoDispatcher
import com.seungsu.ohmysubway.domain.di.MainDispatcher
import com.seungsu.ohmysubway.domain.di.MainImmediateDispatcher

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @DefaultDispatcher fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides @MainDispatcher fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    @Provides @MainImmediateDispatcher fun provideMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
