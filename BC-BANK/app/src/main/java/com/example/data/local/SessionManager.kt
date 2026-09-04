package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("solesfin_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_DNI = "user_dni"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ACCOUNT_TYPE = "account_type"
        private const val KEY_REMEMBER_SESSION = "remember_session"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_USER_PIN = "user_pin"
        private const val KEY_BALANCE_HIDDEN = "is_balance_hidden"
        private const val KEY_EMAIL_VERIFIED = "email_verified"
        private const val KEY_HAS_LAUNCHED = "has_launched"
        private const val KEY_PROFILE_COMPLETED = "profile_completed"
        private const val KEY_BUSINESS_NAME = "business_name"
        private const val KEY_BUSINESS_RUC = "business_ruc"
        private const val KEY_PUSH_NOTIFICATIONS_ENABLED = "push_notifications_enabled"
    }

    fun isPushNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_PUSH_NOTIFICATIONS_ENABLED, false)

    fun setPushNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getUserRole(): String = "user"

    fun isProfileComplete(): Boolean {
        if (prefs.getBoolean(KEY_PROFILE_COMPLETED, false)) return true
        val name = getUserName().trim()
        val dni = getUserDni().trim()
        return name.isNotBlank() && name != "Usuario BC-BANK" && dni.isNotBlank()
    }

    fun setProfileCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETED, completed).apply()
    }

    fun getBusinessName(): String = prefs.getString(KEY_BUSINESS_NAME, "") ?: ""
    fun setBusinessName(name: String) {
        prefs.edit().putString(KEY_BUSINESS_NAME, name).apply()
    }

    fun getBusinessRuc(): String = prefs.getString(KEY_BUSINESS_RUC, "") ?: ""
    fun setBusinessRuc(ruc: String) {
        prefs.edit().putString(KEY_BUSINESS_RUC, ruc).apply()
    }

    fun isBalanceHidden(): Boolean = prefs.getBoolean(KEY_BALANCE_HIDDEN, false)
    fun setBalanceHidden(hidden: Boolean) {
        prefs.edit().putBoolean(KEY_BALANCE_HIDDEN, hidden).apply()
    }

    fun isLoggedIn(): Boolean {
        // If user opted to remember session and has an active login
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun isEmailVerified(): Boolean = prefs.getBoolean(KEY_EMAIL_VERIFIED, false)

    fun setEmailVerified(verified: Boolean) {
        prefs.edit().putBoolean(KEY_EMAIL_VERIFIED, verified).apply()
    }

    fun setLoggedIn(
        loggedIn: Boolean,
        dni: String = "",
        phone: String = "",
        email: String = "",
        name: String = "",
        accountType: String = "",
        pin: String = "",
        remember: Boolean = true
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            if (loggedIn) {
                if (dni.isNotEmpty()) putString(KEY_USER_DNI, dni)
                if (phone.isNotEmpty()) putString(KEY_USER_PHONE, phone)
                if (email.isNotEmpty()) putString(KEY_USER_EMAIL, email)
                if (name.isNotEmpty()) putString(KEY_USER_NAME, name)
                if (accountType.isNotEmpty()) putString(KEY_ACCOUNT_TYPE, accountType)
                if (pin.isNotEmpty()) putString(KEY_USER_PIN, pin)
                putBoolean(KEY_REMEMBER_SESSION, remember)
            } else if (!remember) {
                remove(KEY_USER_DNI)
                remove(KEY_USER_PHONE)
                remove(KEY_USER_EMAIL)
            }
            apply()
        }
    }

    fun getUserDni(): String = prefs.getString(KEY_USER_DNI, "") ?: ""
    fun setUserDni(dni: String) {
        prefs.edit().putString(KEY_USER_DNI, dni).apply()
    }

    fun getUserPhone(): String = prefs.getString(KEY_USER_PHONE, "") ?: ""
    fun setUserPhone(phone: String) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun setUserEmail(email: String) {
        prefs.edit().putString(KEY_USER_EMAIL, email).apply()
    }

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getAccountType(): String = prefs.getString(KEY_ACCOUNT_TYPE, "Cuenta de Ahorros BC-BANK") ?: "Cuenta de Ahorros BC-BANK"
    fun setAccountType(accountType: String) {
        prefs.edit().putString(KEY_ACCOUNT_TYPE, accountType).apply()
    }

    fun isRememberSession(): Boolean = prefs.getBoolean(KEY_REMEMBER_SESSION, true)
    fun setRememberSession(remember: Boolean) {
        prefs.edit().putBoolean(KEY_REMEMBER_SESSION, remember).apply()
    }

    fun getUserPin(): String = prefs.getString(KEY_USER_PIN, "")?.trim() ?: ""

    fun setUserPin(pin: String) {
        prefs.edit().putString(KEY_USER_PIN, pin.trim()).apply()
    }

    fun validatePin(inputPin: String): Boolean {
        val cleanInput = inputPin.trim()
        val currentPin = getUserPin().trim()
        if (currentPin.isBlank()) {
            return (cleanInput.length == 6 || cleanInput.length >= 4) && cleanInput.all { it.isDigit() }
        }
        return cleanInput == currentPin
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    
    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)

    fun clearSession() {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_DNI)
            remove(KEY_USER_PHONE)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_ACCOUNT_TYPE)
            remove(KEY_USER_PIN)
            remove(KEY_EMAIL_VERIFIED)
            apply()
        }
    }
}
