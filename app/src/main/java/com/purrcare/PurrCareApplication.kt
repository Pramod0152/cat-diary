package com.purrcare

import android.app.Application
import androidx.room.Room
import com.purrcare.data.database.PurrCareDatabase
import com.purrcare.notification.NotificationHelper

class PurrCareApplication : Application() {

    val database: PurrCareDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            PurrCareDatabase::class.java,
            "purrcare.db"
        ).fallbackToDestructiveMigration().build()
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
