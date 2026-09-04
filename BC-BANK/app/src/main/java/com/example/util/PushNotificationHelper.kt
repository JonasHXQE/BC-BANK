package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PushNotificationHelper {
    const val CHANNEL_TRANSACTIONS = "bcbank_transactions_channel"
    const val CHANNEL_SECURITY = "bcbank_security_channel"

    fun initializeChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val txChannel = NotificationChannel(
                CHANNEL_TRANSACTIONS,
                "Transacciones y Movimientos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de transferencias, depósitos y retiros en BC-BANK"
                enableVibration(true)
            }

            val secChannel = NotificationChannel(
                CHANNEL_SECURITY,
                "Seguridad y Accesos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas de seguridad, verificación y accesos de cuenta"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(txChannel, secChannel))
        }
    }
}
