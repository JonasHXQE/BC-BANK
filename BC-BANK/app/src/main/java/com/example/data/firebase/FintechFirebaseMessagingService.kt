package com.example.data.firebase

import android.util.Log
import com.example.data.local.SessionManager
import com.example.ui.util.BankNotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Service that handles Firebase Cloud Messaging (FCM) background/foreground push notifications.
 * Works even when the app is in the background or killed.
 */
class FintechFirebaseMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token received: $token")
        val currentUid = FirebaseManager.getCurrentUserUid()
        if (!currentUid.isNullOrBlank() && currentUid != "local_user") {
            scope.launch {
                FirebaseManager.updateFcmToken(currentUid, token)
            }
        }
    }

    companion object {
        private const val TAG = "FintechFCMService"
        private val processedMessageIds = java.util.Collections.newSetFromMap(
            java.util.concurrent.ConcurrentHashMap<String, Boolean>()
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        // 1. Deduplicate by FCM unique messageId
        val msgId = remoteMessage.messageId
        if (!msgId.isNullOrBlank()) {
            if (processedMessageIds.contains(msgId)) {
                Log.d(TAG, "FCM Message already processed ($msgId). Dropping duplicate.")
                return
            }
            processedMessageIds.add(msgId)
            if (processedMessageIds.size > 100) {
                processedMessageIds.clear()
            }
        }

        val sessionManager = SessionManager(applicationContext)
        // Respect user's push notification toggle setting
        if (!sessionManager.isPushNotificationsEnabled()) {
            Log.d(TAG, "Push notifications disabled by user in settings. Skipping alert.")
            return
        }

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title
            ?: data["title"]
            ?: "BC-BANK • Notificación Bancaria"

        val body = notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: "Tienes una nueva actualización en tu cuenta BC-BANK."

        val category = data["category"] ?: "Operaciones"
        val amountTag = data["amountTag"]
        val notificationId = data["notificationId"]?.toIntOrNull() ?: 0

        BankNotificationManager.showSystemNotification(
            context = applicationContext,
            title = title,
            message = body,
            category = category,
            amountTag = amountTag,
            notificationId = notificationId
        )
    }
}
