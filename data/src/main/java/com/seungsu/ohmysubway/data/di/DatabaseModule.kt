package com.seungsu.ohmysubway.data.di

// TODO: Room DB 사용 시 아래 주석 해제
// import android.content.Context
// import androidx.room.Room
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import com.seungsu.ohmysubway.data.local.AppDatabase
// import javax.inject.Singleton
//
// @Module
// @InstallIn(SingletonComponent::class)
// object DatabaseModule {
//
//     @Provides @Singleton
//     fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
//         Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
//             .fallbackToDestructiveMigration()
//             .build()
// }
