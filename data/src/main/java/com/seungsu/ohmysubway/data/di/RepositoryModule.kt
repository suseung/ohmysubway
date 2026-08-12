package com.seungsu.ohmysubway.data.di

import com.seungsu.ohmysubway.data.repository.ArrivalRepositoryImpl
import com.seungsu.ohmysubway.data.repository.SubwayLineRepositoryImpl
import com.seungsu.ohmysubway.domain.repository.ArrivalRepository
import com.seungsu.ohmysubway.domain.repository.SubwayLineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindSubwayLineRepository(impl: SubwayLineRepositoryImpl): SubwayLineRepository

    @Binds
    abstract fun bindArrivalRepository(impl: ArrivalRepositoryImpl): ArrivalRepository
}
