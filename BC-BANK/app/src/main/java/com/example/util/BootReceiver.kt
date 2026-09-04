package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        Log.d(TAG, "Received broadcast action: $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                try {
                    // Initialize notification channels upon reboot or package replacement
                    PushNotificationHelper.initializeChannels(context.applicationContext)
                    Log.d(TAG, "Notification channels initialized after $action")
                } catch (e: Exception) {
                    Log.e(TAG, "Error handling boot event", e)
                }
            }
        }
    }
}
