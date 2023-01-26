package com.example.canteenchecker.adminapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

// wie utility class
private const val CANTEEN_CHANGED_INTENT_ACTION = "CanteenChanged"
private const val CANTEEN_CHANGED_INTENT_CANTEEN_ID_KEY = "CanteenId"

fun Context.sendCanteenChangedBroadcast(canteenId: String){
    val intent = Intent(CANTEEN_CHANGED_INTENT_ACTION) // impliziter intent
    intent.putExtra(CANTEEN_CHANGED_INTENT_CANTEEN_ID_KEY, canteenId)
    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
}

fun Context.registerCanteenChangedBroadcastReceiver(broadcastReceiver: CanteenChangedBroadcastReceiver){
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
        IntentFilter(CANTEEN_CHANGED_INTENT_ACTION)
    )
}

fun Context.unregisterCanteenChangedBroadcastReceiver(broadcastReceiver: CanteenChangedBroadcastReceiver){
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
}

abstract class CanteenChangedBroadcastReceiver : BroadcastReceiver(){
    abstract fun onReceiveCanteenChanged(canteenId: String)

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.getStringExtra(CANTEEN_CHANGED_INTENT_CANTEEN_ID_KEY)?.let{
            onReceiveCanteenChanged(it)
        }
    }
}