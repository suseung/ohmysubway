package com.seungsu.ohmysubway.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // 예시:
    // @Binds abstract fun bindSampleRepository(impl: SampleRepositoryImpl): SampleRepository
}
