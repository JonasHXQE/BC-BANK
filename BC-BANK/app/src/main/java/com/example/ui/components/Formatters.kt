package com.example.ui.components

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    private val decimalSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val solFormat = DecimalFormat("#,##0.00", decimalSymbols)
    private val compactFormat = DecimalFormat("#,##0", decimalSymbols)
    private val dateFormat = SimpleDateFormat("dd MMM, yyyy • hh:mm a", Locale("es", "PE"))
    private val shortDateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale("es", "PE"))

    /**
     * Formats amount to Peruvian Soles e.g. S/ 1,250.00
     */
    fun formatSoles(amount: Double): String {
        return "S/ ${solFormat.format(amount)}"
    }

    fun formatAmountOnly(amount: Double): String {
        return solFormat.format(amount)
    }

    /**
     * Formats amount for privacy mode e.g. S/ ••••••
     */
    fun formatSolesHidden(amount: Double, isHidden: Boolean): String {
        return if (isHidden) "S/ ••••••" else formatSoles(amount)
    }

    /**
     * Formats compact soles for small indicators e.g. S/ 1,250
     */
    fun formatSolesCompact(amount: Double): String {
        return "S/ ${compactFormat.format(amount)}"
    }

    fun formatDateTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }
}
