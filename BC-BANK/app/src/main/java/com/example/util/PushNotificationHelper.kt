package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PushNotificationHelper {
    const val CHANNEL_ID_SECURITY = "security_alerts"
    const val CHANNEL_ID_TRANSACTIONS = "transactions"
    const val CHANNEL_ID_SYSTEM = "system_announcements"

    fun initializeChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val securityChannel = NotificationChannel(
                CHANNEL_ID_SECURITY,
                "Alertas de Seguridad",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones críticas de seguridad de la cuenta"
            }

            val transactionChannel = NotificationChannel(
                CHANNEL_ID_TRANSACTIONS,
                "Transacciones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Confirmación de transferencias y depósitos"
            }

            val systemChannel = NotificationChannel(
                CHANNEL_ID_SYSTEM,
                "Avisos del Sistema",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones informativas del banco"
            }

            notificationManager.createNotificationChannels(
                listOf(securityChannel, transactionChannel, systemChannel)
            )
        }
    }
}
