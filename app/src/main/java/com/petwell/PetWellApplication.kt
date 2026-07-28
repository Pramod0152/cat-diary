package com.petwell

import android.app.Application
import androidx.room.Room
import com.petwell.data.database.PetWellDatabase
import com.petwell.notification.NotificationHelper

class PetWellApplication : Application() {

    val database: PetWellDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            PetWellDatabase::class.java,
            "petwell.db"
        ).fallbackToDestructiveMigration().build()
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
