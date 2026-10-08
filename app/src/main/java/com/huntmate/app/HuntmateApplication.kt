package com.huntmate.app

import android.app.Application
import com.google.firebase.FirebaseApp

class HuntmateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
