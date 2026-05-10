package com.superwakeupware.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import androidx.room.Room
import com.superwakeupware.data.AlarmDao
import com.superwakeupware.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "superwake.db").build()

    @Provides @Singleton
    fun provideAlarmDao(db: AppDatabase): AlarmDao = db.alarmDao()

    @Provides @Singleton
    fun provideAlarmManager(@ApplicationContext ctx: Context): AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides @Singleton
    fun provideNotificationManager(@ApplicationContext ctx: Context): NotificationManager =
        ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides @Singleton
    fun providePowerManager(@ApplicationContext ctx: Context): PowerManager =
        ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
}
