package com.example.canteenchecker.adminapp

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CanteenCheckerAdminFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.data["canteenId"]?.let{ canteenId ->
            sendCanteenChangedBroadcast(canteenId)
        }
    }
}