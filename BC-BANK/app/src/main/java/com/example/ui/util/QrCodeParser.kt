package com.example.ui.util

import android.net.Uri

sealed class CustomerQrScanResult {
    data class ValidRecipient(
        val recipientName: String,
        val identifier: String,
        val amount: Double?
    ) : CustomerQrScanResult()

    data class VoucherCodeDetected(
        val serial: String
    ) : CustomerQrScanResult()

    data class WithdrawalCodeDetected(
        val code: String = ""
    ) : CustomerQrScanResult()

    data class PaymentOrCipCodeDetected(
        val typeDesc: String
    ) : CustomerQrScanResult()

    data class NotAValidCustomerQr(
        val reason: String
    ) : CustomerQrScanResult()
}

sealed class QrParsedResult {
    data class Success(val data: String) : QrParsedResult()
    data class Error(val message: String) : QrParsedResult()
}

object QrCodeParser {
    fun parseCustomerTransferQr(rawData: String): CustomerQrScanResult {
        val trimmed = rawData.trim()
        if (trimmed.isEmpty()) {
            return CustomerQrScanResult.NotAValidCustomerQr("El código QR está vacío.")
        }

        // Check if voucher / comprobante
        if (trimmed.startsWith("BCBANK_VOUCHER_") || trimmed.startsWith("VOUCHER:") || trimmed.contains("serial=")) {
            val serial = trimmed.substringAfter("serial=", "").substringBefore("&").ifBlank {
                trimmed.substringAfter("BCBANK_VOUCHER_", "").take(16)
            }
            return CustomerQrScanResult.VoucherCodeDetected(serial.ifBlank { "VCH-0001" })
        }

        // Check if withdrawal
        if (trimmed.startsWith("BCBANK_WITHDRAW_") || trimmed.contains("withdrawalCode")) {
            return CustomerQrScanResult.WithdrawalCodeDetected(trimmed)
        }

        // Check if service payment / CIP
        if (trimmed.startsWith("CIP:") || trimmed.contains("cip=") || trimmed.startsWith("SERVICE_PAY:")) {
            return CustomerQrScanResult.PaymentOrCipCodeDetected("Código de Pago de Servicio CIP")
        }

        // Standard BC-BANK transfer format: bcbank://transfer?recipient=Name&identifier=123456789&amount=50.00
        // Or format: RECIPIENT|IDENTIFIER|AMOUNT
        if (trimmed.contains("|")) {
            val parts = trimmed.split("|")
            if (parts.size >= 2) {
                val name = parts[0].trim()
                val identifier = parts[1].trim()
                val amount = parts.getOrNull(2)?.toDoubleOrNull()
                return CustomerQrScanResult.ValidRecipient(name, identifier, amount)
            }
        }

        if (trimmed.startsWith("bcbank://") || trimmed.contains("bcbank.pe")) {
            try {
                val uri = Uri.parse(trimmed)
                val recipient = uri.getQueryParameter("recipient") ?: uri.getQueryParameter("name") ?: "Usuario BC-BANK"
                val identifier = uri.getQueryParameter("identifier") ?: uri.getQueryParameter("phone") ?: uri.getQueryParameter("account") ?: ""
                val amountStr = uri.getQueryParameter("amount")
                val amount = amountStr?.toDoubleOrNull()
                if (identifier.isNotBlank()) {
                    return CustomerQrScanResult.ValidRecipient(recipient, identifier, amount)
                }
            } catch (_: Exception) {}
        }

        // Simple phone number (9 digits)
        if (trimmed.length == 9 && trimmed.all { it.isDigit() }) {
            return CustomerQrScanResult.ValidRecipient("Contacto BC-BANK", trimmed, null)
        }

        // Simple account or CCI
        if (trimmed.length >= 10 && trimmed.filter { it.isDigit() }.length >= 10) {
            return CustomerQrScanResult.ValidRecipient("Cuenta BC-BANK", trimmed, null)
        }

        return CustomerQrScanResult.NotAValidCustomerQr(
            "El contenido del código QR no corresponde a un formato de cliente BC-BANK válido."
        )
    }
}
