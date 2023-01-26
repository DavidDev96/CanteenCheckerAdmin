package com.example.canteenchecker.adminapp

import android.app.Application
import com.google.firebase.messaging.FirebaseMessaging

class CanteenCheckerAdminApplication : Application() {
    var authenticationToken: String = ""
    var canteenId : String = ""

    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().subscribeToTopic("CanteenUpdates")
    }
}