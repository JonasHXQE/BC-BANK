package com.example.data.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.example.data.local.AccountInfoEntity
import com.example.data.local.BankNotificationEntity
import com.example.data.local.BudgetEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import java.net.Inet4Address
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * FirebaseManager handles cloud authentication, Firestore synchronization,
 * role management (User, Admin, Superadmin), withdrawals with retention,
 * services CRUD, and comprehensive security audit logging for BC-BANK.
 */
object FirebaseManager {
    private const val TAG = "FirebaseManager"
    
    private var _isFirebaseAvailable: Boolean? = null
    private var appContext: Context? = null

    val isFirebaseAvailable: Boolean
        get() {
            if (_isFirebaseAvailable == true) return true
            return try {
                val available = FirebaseApp.getApps(appContext ?: FirebaseApp.getInstance().applicationContext).isNotEmpty()
                _isFirebaseAvailable = available
                available
            } catch (e: Exception) {
                try {
                    val available = FirebaseApp.getInstance() != null
                    _isFirebaseAvailable = available
                    available
                } catch (e2: Exception) {
                    false
                }
            }
        }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                _isFirebaseAvailable = true
                Log.d(TAG, "Firebase initialized successfully")
            } else {
                val app = FirebaseApp.initializeApp(context)
                _isFirebaseAvailable = app != null
                Log.d(TAG, "FirebaseApp initialized: $_isFirebaseAvailable")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase init notice: ${e.message}")
            _isFirebaseAvailable = false
        }
    }

    fun isConfigured(): Boolean = isFirebaseAvailable

    internal fun DocumentSnapshot.getDoubleSafe(field: String, default: Double = 0.0): Double {
        val raw = get(field) ?: return default
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull() ?: default
            else -> default
        }
    }

    internal fun DocumentSnapshot.getLongSafe(field: String, default: Long = 0L): Long {
        val raw = get(field) ?: return default
        return when (raw) {
            is Number -> raw.toLong()
            is String -> raw.toLongOrNull() ?: default
            else -> default
        }
    }

    internal fun DocumentSnapshot.getStringSafe(field: String, default: String = ""): String {
        val raw = get(field) ?: return default
        return when (raw) {
            is String -> raw
            is Number -> raw.toString()
            is Boolean -> raw.toString()
            else -> raw.toString()
        }
    }

    internal fun DocumentSnapshot.getBooleanSafe(field: String, default: Boolean = false): Boolean {
        val raw = get(field) ?: return default
        return when (raw) {
            is Boolean -> raw
            is String -> raw.toBooleanStrictOrNull() ?: default
            is Number -> raw.toInt() != 0
            else -> default
        }
    }

    fun getCurrentUserUid(): String? {
        if (!isFirebaseAvailable) return null
        return try {
            FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        }
    }

    fun getDeviceIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces != null && interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "IP Resolution: ${e.message}")
        }
        return "192.168.1.100"
    }

    fun getNetworkType(): String {
        val ctx = appContext ?: return "Red Móvil / WiFi"
        return try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = cm?.activeNetwork ?: return "Sin Conexión"
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return "Red Activa"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi (Alta Velocidad)"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Red Móvil (4G / 5G LTE)"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet Seguro"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "Túnel VPN Encriptado"
                else -> "Red de Datos Segura"
            }
        } catch (e: Exception) {
            "Red Móvil / WiFi"
        }
    }

    fun getDeviceModel(): String = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    suspend fun recordSecurityAuditLog(
        uid: String,
        eventType: String,
        details: String,
        status: String = "SUCCESS",
        userEmail: String = "",
        dni: String = ""
    ) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            val now = System.currentTimeMillis()
            val logId = "LOG_${now}_${UUID.randomUUID().toString().take(6).uppercase()}"
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
            val dateDayFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormatter.format(Date(now))
            val dateDay = dateDayFormatter.format(Date(now))
            val ipAddress = getDeviceIpAddress()
            val netType = getNetworkType()
            val deviceModel = getDeviceModel()
            val osVersion = getAndroidVersion()

            val logData = hashMapOf(
                "logId" to logId,
                "uid" to uid,
                "eventType" to eventType,
                "timestamp" to now,
                "formattedDate" to formattedDate,
                "dateDay" to dateDay,
                "ipAddress" to ipAddress,
                "networkType" to netType,
                "deviceModel" to deviceModel,
                "androidVersion" to osVersion,
                "details" to details,
                "status" to status,
                "userEmail" to userEmail,
                "dni" to dni
            )

            db.collection("users").document(uid).collection("security_audit_logs").document(logId)
                .set(logData, SetOptions.merge()).await()

            db.collection("security_logs_by_date").document(dateDay).collection("events").document(logId)
                .set(logData, SetOptions.merge()).await()

            Log.d(TAG, "Security audit log recorded: [$eventType] $details - IP: $ipAddress")
        } catch (e: Exception) {
            Log.w(TAG, "Security audit log notice: ${e.message}")
        }
    }

    suspend fun findUserDocByDniOrCip(identifier: String): DocumentSnapshot? {
        if (!isFirebaseAvailable) return null
        return try {
            val db = FirebaseFirestore.getInstance()
            val clean = identifier.trim()
            val queryDni = db.collection("users").whereEqualTo("dni", clean).limit(1).get().await()
            if (!queryDni.isEmpty) {
                return queryDni.documents.first()
            }
            val queryEmail = db.collection("users").whereEqualTo("email", clean).limit(1).get().await()
            if (!queryEmail.isEmpty) {
                return queryEmail.documents.first()
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "findUserDocByDniOrCip: ${e.message}")
            null
        }
    }

    suspend fun signInWithFirebase(emailOrDni: String, pass: String): Result<Pair<String, String>> {
        if (!isFirebaseAvailable) {
            return Result.success(Pair("LOCAL_SESSION_USER", emailOrDni))
        }
        return try {
            var actualEmail = emailOrDni.trim()
            val db = FirebaseFirestore.getInstance()

            // If user entered DNI or other identifier instead of email, look it up in Firestore first
            if (!actualEmail.contains("@")) {
                val userDoc = findUserDocByDniOrCip(actualEmail)
                if (userDoc != null && userDoc.contains("email")) {
                    actualEmail = userDoc.getString("email") ?: actualEmail
                } else {
                    return Result.failure(Exception("No se encontró ninguna cuenta asociada al DNI $emailOrDni. Verifica tu documento o ingresa con tu correo."))
                }
            }

            val auth = FirebaseAuth.getInstance()
            val authResult = auth.signInWithEmailAndPassword(actualEmail, pass).await()
            val user = authResult.user
            val uid = user?.uid ?: "LOCAL_UID"

            recordSecurityAuditLog(
                uid = uid,
                eventType = "LOGIN_SUCCESS",
                details = "Inicio de sesión autenticado correctamente para $actualEmail",
                status = "SUCCESS",
                userEmail = actualEmail
            )
            Result.success(Pair(uid, actualEmail))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth SignIn: ${e.message}")
            recordSecurityAuditLog(
                uid = "UNAUTHENTICATED",
                eventType = "LOGIN_FAILED",
                details = "Fallo en autenticación para $emailOrDni: ${e.message}",
                status = "FAILED",
                userEmail = emailOrDni
            )
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleToken(idToken: String): Result<Pair<String, String>> {
        if (!isFirebaseAvailable) {
            return Result.success(Pair("GOOGLE_LOCAL_UID", "user@gmail.com"))
        }
        return try {
            val auth = FirebaseAuth.getInstance()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            val uid = user?.uid ?: "GOOGLE_UID"
            val email = user?.email ?: ""
            val displayName = user?.displayName ?: ""

            try {
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(uid).get().await()
                if (!userDoc.exists()) {
                    val now = System.currentTimeMillis()
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val initialData = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "fullName" to displayName.ifBlank { "Usuario Google" },
                        "accountType" to "Cuenta de Ahorros BC-BANK",
                        "role" to "user",
                        "emailVerified" to true,
                        "profileComplete" to false,
                        "createdAt" to now,
                        "createdAtFormatted" to dateFormatter.format(Date(now)),
                        "status" to "ACTIVE"
                    )
                    db.collection("users").document(uid).set(initialData, SetOptions.merge()).await()
                }
            } catch (dbError: Exception) {
                Log.w(TAG, "Firestore user creation on Google Sign-In notice: ${dbError.message}")
            }

            recordSecurityAuditLog(
                uid = uid,
                eventType = "LOGIN_GOOGLE_SUCCESS",
                details = "Inicio de sesión exitoso con Google para $email",
                status = "SUCCESS",
                userEmail = email
            )
            Result.success(Pair(uid, email))
        } catch (e: Exception) {
            Log.e(TAG, "signInWithGoogleToken Error: ${e.message}")
            recordSecurityAuditLog(
                uid = "UNAUTHENTICATED",
                eventType = "LOGIN_GOOGLE_FAILED",
                details = "Error en Google Sign-In: ${e.message}",
                status = "FAILED"
            )
            Result.failure(e)
        }
    }

    suspend fun registerWithFirebase(
        email: String,
        pass: String,
        fullName: String = "",
        dni: String = "",
        phone: String = "",
        accountType: String = "Cuenta de Ahorros BC-BANK"
    ): Result<String> {
        if (!isFirebaseAvailable) {
            return Result.success("LOCAL_REGISTERED_USER")
        }
        return try {
            val auth = FirebaseAuth.getInstance()
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user
            val uid = user?.uid ?: "LOCAL_UID"
            
            try {
                user?.sendEmailVerification()?.await()
                recordSecurityAuditLog(
                    uid = uid,
                    eventType = "EMAIL_VERIFICATION_SENT",
                    details = "Enlace de confirmación enviado a $email",
                    status = "PENDING",
                    userEmail = email,
                    dni = dni
                )
            } catch (evErr: Exception) {
                Log.w(TAG, "Notice sending email verification: ${evErr.message}")
            }

            try {
                val db = FirebaseFirestore.getInstance()
                val now = System.currentTimeMillis()
                val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val userData = hashMapOf(
                    "uid" to uid,
                    "fullName" to fullName.ifBlank { "Usuario BC-BANK" },
                    "dni" to dni,
                    "phone" to phone,
                    "email" to email,
                    "accountType" to accountType,
                    "role" to "user",
                    "initialBalance" to 0.00,
                    "createdAt" to now,
                    "createdAtFormatted" to dateFormatter.format(Date(now)),
                    "registrationIp" to getDeviceIpAddress(),
                    "registrationNetwork" to getNetworkType(),
                    "registrationDevice" to getDeviceModel(),
                    "androidVersion" to getAndroidVersion(),
                    "emailVerified" to (user?.isEmailVerified == true),
                    "profileComplete" to (fullName.isNotBlank() && dni.isNotBlank()),
                    "status" to "ACTIVE"
                )
                db.collection("users").document(uid).set(userData, SetOptions.merge()).await()
            } catch (fsError: Exception) {
                Log.w(TAG, "Firestore sync pending: ${fsError.message}")
            }

            recordSecurityAuditLog(
                uid = uid,
                eventType = "REGISTRATION",
                details = "Registro de nuevo usuario para $email",
                status = "SUCCESS",
                userEmail = email,
                dni = dni
            )
            
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth Register: ${e.message}")
            recordSecurityAuditLog(
                uid = "UNREGISTERED",
                eventType = "REGISTRATION_FAILED",
                details = "Error al intentar registrar cuenta para $email: ${e.message}",
                status = "FAILED",
                userEmail = email,
                dni = dni
            )
            Result.failure(e)
        }
    }

    suspend fun checkIdentityAvailability(uid: String, dni: String, phone: String): Result<Boolean> {
        if (!isFirebaseAvailable) return Result.success(true)
        return try {
            val db = FirebaseFirestore.getInstance()
            val dniClean = dni.trim()
            val phoneClean = phone.trim()

            // 1. Check in identities collection by DNI doc
            if (dniClean.isNotBlank()) {
                val dniDoc = db.collection("identities").document(dniClean).get().await()
                if (dniDoc.exists()) {
                    val registeredUid = dniDoc.getString("uid")
                    if (registeredUid != null && registeredUid != uid) {
                        return Result.failure(Exception("El DNI $dniClean ya se encuentra registrado y vinculado a otra cuenta bancaria en BC-BANK."))
                    }
                }

                // Check users collection
                val userDniQuery = db.collection("users").whereEqualTo("dni", dniClean).get().await()
                for (doc in userDniQuery.documents) {
                    if (doc.id != uid) {
                        return Result.failure(Exception("El DNI $dniClean ya se encuentra registrado en otra cuenta activa."))
                    }
                }
            }

            // 2. Check in identities collection or users by phone
            if (phoneClean.isNotBlank()) {
                val phoneQuery = db.collection("identities").whereEqualTo("phone", phoneClean).get().await()
                for (doc in phoneQuery.documents) {
                    val registeredUid = doc.getString("uid")
                    if (registeredUid != null && registeredUid != uid) {
                        return Result.failure(Exception("El número de teléfono $phoneClean ya se encuentra asociado a otra cuenta bancaria en BC-BANK."))
                    }
                }

                val userPhoneQuery = db.collection("users").whereEqualTo("phone", phoneClean).get().await()
                for (doc in userPhoneQuery.documents) {
                    if (doc.id != uid) {
                        return Result.failure(Exception("El número de celular $phoneClean ya se encuentra registrado en otra cuenta activa."))
                    }
                }
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Identity availability check notice: ${e.message}")
            Result.success(true)
        }
    }

    suspend fun completeUserProfile(
        uid: String,
        accountType: String,
        fullName: String,
        dni: String,
        phone: String,
        pin: String = "",
        isBusiness: Boolean = false,
        businessName: String = "",
        businessRuc: String = "",
        birthDate: String = "",
        isKid: Boolean = false,
        age: Int = 0
    ): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val availability = checkIdentityAvailability(uid, dni, phone)
            if (availability.isFailure) {
                return Result.failure(availability.exceptionOrNull() ?: Exception("Identidad ya registrada"))
            }

            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val email = auth.currentUser?.email ?: ""
            val now = System.currentTimeMillis()
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormatter.format(Date(now))

            // Generate an official non-DNI CIP code for deposits
            val cleanCip = generateCleanCipNumber(uid)
            val cipCode = "CIP-${cleanCip.take(4)}-${cleanCip.takeLast(4)}"

            val updateData = hashMapOf<String, Any>(
                "uid" to uid,
                "accountType" to accountType,
                "fullName" to fullName,
                "dni" to dni,
                "phone" to phone,
                "email" to email,
                "cipCode" to cipCode,
                "isBusiness" to isBusiness,
                "businessName" to businessName,
                "businessRuc" to businessRuc,
                "birthDate" to birthDate,
                "isKid" to isKid,
                "age" to age,
                "role" to "user",
                "profileComplete" to true,
                "emailVerified" to true,
                "updatedAt" to now,
                "updatedAtFormatted" to formattedDate,
                "status" to "ACTIVE"
            )

            if (pin.isNotBlank()) {
                updateData["securityPin"] = pin
                updateData["pinUpdatedAt"] = now
                updateData["pinUpdatedAtFormatted"] = formattedDate

                // Store in dedicated security_pins collection
                val pinData = hashMapOf<String, Any>(
                    "uid" to uid,
                    "pin" to pin,
                    "status" to "ACTIVE",
                    "failedAttempts" to 0,
                    "pinBlockedUntil" to 0L,
                    "createdAt" to now,
                    "createdAtFormatted" to formattedDate,
                    "updatedAt" to now,
                    "updatedAtFormatted" to formattedDate,
                    "lastVerifiedAt" to now,
                    "version" to 1
                )
                db.collection("security_pins").document(uid).set(pinData, SetOptions.merge()).await()
            }

            db.collection("users").document(uid).set(updateData, SetOptions.merge()).await()

            // Also register in top-level cip_codes collection
            val qrData = "BCBANK_CIP:$cleanCip|ACC:PENDING|HOLDER:$fullName|DNI:$dni"
            val cipData = hashMapOf<String, Any>(
                "cipCode" to cipCode,
                "cleanCode" to cleanCip,
                "uid" to uid,
                "accountHolder" to fullName,
                "userDni" to dni,
                "userPhone" to phone,
                "userEmail" to email,
                "accountNumber" to "",
                "cciNumber" to "",
                "bankName" to "BC-BANK Perú",
                "status" to "ACTIVE",
                "currency" to "PEN",
                "totalDeposited" to 0.0,
                "createdAt" to now,
                "createdAtFormatted" to formattedDate,
                "updatedAt" to now,
                "qrData" to qrData
            )
            db.collection("cip_codes").document(cipCode).set(cipData, SetOptions.merge()).await()

            // Also store identity record for fast uniqueness verification and admin querying
            if (dni.isNotBlank()) {
                val identData = hashMapOf(
                    "dni" to dni,
                    "phone" to phone,
                    "fullName" to fullName,
                    "email" to email,
                    "uid" to uid,
                    "cipCode" to cipCode,
                    "accountType" to accountType,
                    "isBusiness" to isBusiness,
                    "businessName" to businessName,
                    "businessRuc" to businessRuc,
                    "birthDate" to birthDate,
                    "isKid" to isKid,
                    "age" to age,
                    "updatedAt" to now
                )
                db.collection("identities").document(dni).set(identData, SetOptions.merge()).await()
            }
            
            recordSecurityAuditLog(
                uid = uid,
                eventType = "PROFILE_COMPLETED",
                details = "Perfil y cuenta bancaria activada ($accountType) para $fullName ($dni). CIP asignado: $cipCode",
                status = "SUCCESS",
                userEmail = email,
                dni = dni
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "CompleteUserProfile Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(uid: String): UserProfileData? {
        if (!isFirebaseAvailable) return null
        return try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid).get().await()
            if (!userDoc.exists()) return null
            
            val email = userDoc.getString("email") ?: (FirebaseAuth.getInstance().currentUser?.email ?: "")

            UserProfileData(
                uid = uid,
                fullName = userDoc.getString("fullName") ?: "Usuario BC-BANK",
                dni = userDoc.getString("dni") ?: "",
                phone = userDoc.getString("phone") ?: "",
                email = email,
                accountType = userDoc.getString("accountType") ?: "Cuenta de Ahorros BC-BANK",
                role = "user",
                isBusiness = userDoc.getBoolean("isBusiness") ?: false,
                businessName = userDoc.getString("businessName") ?: "",
                businessRuc = userDoc.getString("businessRuc") ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "getUserProfile error: ${e.message}")
            null
        }
    }

    suspend fun updateUserPhone(uid: String, newPhone: String, dni: String = ""): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).set(
                hashMapOf("phone" to newPhone, "updatedAt" to System.currentTimeMillis()),
                SetOptions.merge()
            ).await()

            if (dni.isNotBlank()) {
                try {
                    db.collection("identities").document(dni).set(
                        hashMapOf("phone" to newPhone, "updatedAt" to System.currentTimeMillis()),
                        SetOptions.merge()
                    ).await()
                } catch (idE: Exception) {
                    Log.w(TAG, "Identity phone sync notice: ${idE.message}")
                }
            }

            recordSecurityAuditLog(
                uid = uid,
                eventType = "PHONE_UPDATED",
                details = "Número de celular actualizado a $newPhone"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(uid: String, token: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).set(
                hashMapOf(
                    "fcmToken" to token,
                    "lastFcmTokenUpdate" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
            Log.d(TAG, "FCM Token synchronized for user $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "updateFcmToken notice: ${e.message}")
            Result.failure(e)
        }
    }

    fun syncFcmTokenSafely(uid: String) {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isNullOrBlank()) {
                        val token = task.result
                        Log.d(TAG, "FCM token retrieved safely for user $uid")
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            updateFcmToken(uid, token)
                        }
                    } else {
                        Log.w(TAG, "FCM registration task not completed: ${task.exception?.message}")
                    }
                }
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseMessaging not available or failed safely: ${e.message}")
        }
    }

    fun listenToUserAccountStatus(
        uid: String,
        onStatusChanged: (AccountSecurityStatus) -> Unit
    ): ListenerRegistration? {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return null
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "listenToUserAccountStatus error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val rawStatus = snapshot.getString("status")?.uppercase() ?: "ACTIVE"
                    val suspensionReason = snapshot.getString("suspensionReason")
                        ?: "Tu cuenta se encuentra en revisión de seguridad preventiva por el banco."
                    val suspendedUntil = snapshot.getLong("suspendedUntil")
                    val blockReason = snapshot.getString("blockReason")
                        ?: "Cuenta bloqueada permanentemente por infringir las políticas de seguridad bancaria."
                    val blockedAt = snapshot.getLong("blockedAt") ?: System.currentTimeMillis()
                    val supportContact = snapshot.getString("supportContact") ?: "soporte@bcbank.pe"
                    val supportPhone = snapshot.getString("supportPhone") ?: "+51 900 000 000"

                    val now = System.currentTimeMillis()
                    val resolvedStatus = when (rawStatus) {
                        "SUSPENDED" -> {
                            if (suspendedUntil != null && suspendedUntil > 0 && now >= suspendedUntil) {
                                AccountStatusType.ACTIVE
                            } else {
                                AccountStatusType.SUSPENDED
                            }
                        }
                        "BLOCKED" -> AccountStatusType.BLOCKED
                        else -> AccountStatusType.ACTIVE
                    }

                    onStatusChanged(
                        AccountSecurityStatus(
                            status = resolvedStatus,
                            title = when (resolvedStatus) {
                                AccountStatusType.SUSPENDED -> snapshot.getString("suspensionTitle") ?: "Cuenta Temporalmente Suspendida"
                                AccountStatusType.BLOCKED -> snapshot.getString("blockTitle") ?: "Cuenta Bloqueada por Seguridad"
                                else -> "Cuenta Activa"
                            },
                            reason = if (resolvedStatus == AccountStatusType.SUSPENDED) suspensionReason else blockReason,
                            suspendedUntil = suspendedUntil,
                            blockedAt = blockedAt,
                            supportContact = supportContact,
                            supportPhone = supportPhone
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering user status listener: ${e.message}")
            null
        }
    }

    fun listenToWithdrawals(
        uid: String,
        onWithdrawalChanged: (withdrawal: CloudWithdrawal, isInitialLoad: Boolean, previousStatus: String?) -> Unit
    ): ListenerRegistration? {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return null
        val previousStatuses = mutableMapOf<String, String>()
        var isFirstSnapshot = true
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("withdrawals")
                .whereEqualTo("uid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val currentIsFirst = isFirstSnapshot
                    isFirstSnapshot = false

                    for (change in snapshot.documentChanges) {
                        try {
                            val w = mapWithdrawalDoc(change.document)
                            val prevStatus = previousStatuses[w.id]
                            previousStatuses[w.id] = w.status
                            onWithdrawalChanged(w, currentIsFirst, prevStatus)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error mapping withdrawal change: ${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "listenToWithdrawals error: ${e.message}")
            null
        }
    }

    fun generateCleanCipNumber(seed: String): String {
        val cleanSeed = seed.ifBlank { UUID.randomUUID().toString() }
        val hash = (cleanSeed.hashCode().toLong() and 0xFFFFFFFFL) xor 0x5DEECE66DL
        val number = 10000000L + (Math.abs(hash * 31L + 7919L) % 89999999L)
        return number.toString().padStart(8, '0').take(8)
    }

    fun generateCleanCipCode(seed: String): String {
        val num = generateCleanCipNumber(seed)
        return "CIP-${num.take(4)}-${num.takeLast(4)}"
    }

    suspend fun updateUserPin(uid: String, newPin: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            val now = System.currentTimeMillis()
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormatter.format(Date(now))

            // 1. Dedicated security_pins collection
            val pinDocData = hashMapOf<String, Any>(
                "uid" to uid,
                "pin" to newPin,
                "status" to "ACTIVE",
                "failedAttempts" to 0,
                "pinBlockedUntil" to 0L,
                "updatedAt" to now,
                "updatedAtFormatted" to formattedDate,
                "lastVerifiedAt" to now,
                "version" to 1
            )
            db.collection("security_pins").document(uid).set(pinDocData, SetOptions.merge()).await()

            // 2. Sync to user profile document
            db.collection("users").document(uid).set(
                hashMapOf(
                    "securityPin" to newPin,
                    "pinUpdatedAt" to now,
                    "pinUpdatedAtFormatted" to formattedDate
                ),
                SetOptions.merge()
            ).await()

            recordSecurityAuditLog(
                uid = uid,
                eventType = "PIN_UPDATED",
                details = "PIN de seguridad bancario de 6 dígitos actualizado en la colección dedicada security_pins y sincronizado"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateUserPin error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getUserSecurityPin(uid: String): String? {
        if (!isFirebaseAvailable || uid.isBlank()) return null
        return try {
            val db = FirebaseFirestore.getInstance()
            val pinDoc = db.collection("security_pins").document(uid).get().await()
            val pinFromDedicated = if (pinDoc.exists()) {
                val p = pinDoc.getStringSafe("pin", "")
                if (p.isNotBlank()) p else {
                    val num = pinDoc.getLongSafe("pin", -1L)
                    if (num >= 0) num.toString().padStart(6, '0') else null
                }
            } else null

            if (!pinFromDedicated.isNullOrBlank()) {
                pinFromDedicated.trim()
            } else {
                val userDoc = db.collection("users").document(uid).get().await()
                val pinFromUser = userDoc.getStringSafe("securityPin", "")
                if (pinFromUser.isNotBlank()) {
                    pinFromUser.trim()
                } else {
                    val num = userDoc.getLongSafe("securityPin", -1L)
                    if (num >= 0) num.toString().padStart(6, '0') else null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "getUserSecurityPin error: ${e.message}")
            null
        }
    }

    suspend fun syncUserCipCodeToFirestore(
        uid: String,
        fullName: String,
        dni: String,
        phone: String = "",
        email: String = "",
        accountNumber: String = "",
        cciNumber: String = ""
    ): Result<String> {
        if (!isFirebaseAvailable || uid.isBlank()) {
            val cleanCip = generateCleanCipNumber(uid)
            return Result.success("CIP-${cleanCip.take(4)}-${cleanCip.takeLast(4)}")
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid).get().await()
            var existingCip = userDoc.getString("cipCode")

            val cleanCip = if (!existingCip.isNullOrBlank()) {
                existingCip.replace("CIP-", "").replace("-", "").trim()
            } else {
                generateCleanCipNumber(uid)
            }
            val formattedCip = if (!existingCip.isNullOrBlank() && existingCip.startsWith("CIP-")) {
                existingCip
            } else {
                "CIP-${cleanCip.take(4)}-${cleanCip.takeLast(4)}"
            }

            val now = System.currentTimeMillis()
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormatter.format(Date(now))
            val qrData = "BCBANK_CIP:$cleanCip|ACC:${accountNumber.ifBlank { "PENDING" }}|HOLDER:$fullName|DNI:$dni"

            val cipData = hashMapOf<String, Any>(
                "cipCode" to formattedCip,
                "cleanCode" to cleanCip,
                "uid" to uid,
                "accountHolder" to fullName,
                "userDni" to dni,
                "userPhone" to phone,
                "userEmail" to email,
                "accountNumber" to accountNumber,
                "cciNumber" to cciNumber,
                "bankName" to "BC-BANK Perú",
                "status" to "ACTIVE",
                "currency" to "PEN",
                "updatedAt" to now,
                "updatedAtFormatted" to formattedDate,
                "qrData" to qrData
            )

            // Save in cip_codes collection
            db.collection("cip_codes").document(formattedCip).set(cipData, SetOptions.merge()).await()

            // Update user document
            db.collection("users").document(uid).set(
                hashMapOf(
                    "cipCode" to formattedCip,
                    "updatedAt" to now
                ),
                SetOptions.merge()
            ).await()

            Log.d(TAG, "CIP Code $formattedCip synchronized for user $uid")
            Result.success(formattedCip)
        } catch (e: Exception) {
            Log.e(TAG, "syncUserCipCodeToFirestore error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getOrCreateUserCipCode(
        uid: String,
        fullName: String,
        dni: String,
        phone: String = "",
        email: String = "",
        accountNumber: String = "",
        cciNumber: String = ""
    ): String {
        if (!isFirebaseAvailable || uid.isBlank()) {
            val clean = generateCleanCipNumber(uid)
            return "CIP-${clean.take(4)}-${clean.takeLast(4)}"
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            val userDoc = db.collection("users").document(uid).get().await()
            val userCip = userDoc.getString("cipCode")
            if (!userCip.isNullOrBlank() && userCip.startsWith("CIP-")) {
                userCip
            } else {
                syncUserCipCodeToFirestore(
                    uid = uid,
                    fullName = fullName,
                    dni = dni,
                    phone = phone,
                    email = email,
                    accountNumber = accountNumber,
                    cciNumber = cciNumber
                ).getOrNull() ?: generateCleanCipCode(uid)
            }
        } catch (e: Exception) {
            generateCleanCipCode(uid)
        }
    }

    // --- WITHDRAWALS WITH RETENTION & 1-HOUR EXPIRATION ---
    suspend fun createPendingWithdrawal(
        uid: String,
        userDni: String,
        userPhone: String,
        accountHolder: String,
        amount: Double,
        pinCode: String,
        opCode: String,
        qrData: String
    ): Result<CloudWithdrawal> {
        val now = System.currentTimeMillis()
        val expiresAt = now + (60 * 60 * 1000L) // 1 hour retention
        val withdrawalId = "WDR_${now}_${UUID.randomUUID().toString().take(6).uppercase()}"

        val withdrawal = CloudWithdrawal(
            id = withdrawalId,
            uid = uid,
            userDni = userDni,
            userPhone = userPhone,
            accountHolder = accountHolder,
            amount = amount,
            pinCode = pinCode,
            opCode = opCode,
            qrData = qrData,
            status = "PENDING",
            createdAt = now,
            expiresAt = expiresAt
        )

        if (!isFirebaseAvailable) {
            return Result.success(withdrawal)
        }

        return try {
            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "id" to withdrawal.id,
                "uid" to withdrawal.uid,
                "userDni" to withdrawal.userDni,
                "userPhone" to withdrawal.userPhone,
                "accountHolder" to withdrawal.accountHolder,
                "amount" to withdrawal.amount,
                "pinCode" to withdrawal.pinCode,
                "opCode" to withdrawal.opCode,
                "qrData" to withdrawal.qrData,
                "status" to withdrawal.status,
                "createdAt" to withdrawal.createdAt,
                "expiresAt" to withdrawal.expiresAt
            )
            db.collection("withdrawals").document(withdrawalId).set(data).await()
            recordSecurityAuditLog(
                uid = uid,
                eventType = "WITHDRAWAL_RESERVATION_CREATED",
                details = "Retiro generado por S/ ${String.format(Locale.US, "%.2f", amount)}. Saldo retenido por 1 hora.",
                dni = userDni
            )
            Result.success(withdrawal)
        } catch (e: Exception) {
            Log.e(TAG, "createPendingWithdrawal: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun cancelPendingWithdrawal(withdrawalId: String, uid: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("withdrawals").document(withdrawalId).set(
                hashMapOf("status" to "CANCELLED", "cancelledAt" to System.currentTimeMillis()),
                SetOptions.merge()
            ).await()
            recordSecurityAuditLog(
                uid = uid,
                eventType = "WITHDRAWAL_CANCELLED",
                details = "Retiro $withdrawalId cancelado con PIN. Saldo liberado y reintegrado a cuenta."
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findWithdrawalByCodeOrPin(query: String): CloudWithdrawal? {
        if (!isFirebaseAvailable) return null
        return try {
            val db = FirebaseFirestore.getInstance()
            val clean = query.trim()
            
            // Try by ID
            val docById = db.collection("withdrawals").document(clean).get().await()
            if (docById.exists()) {
                return mapWithdrawalDoc(docById)
            }
            
            // Try by PIN
            val snapPin = db.collection("withdrawals").whereEqualTo("pinCode", clean).limit(1).get().await()
            if (!snapPin.isEmpty) {
                return mapWithdrawalDoc(snapPin.documents.first())
            }

            // Try by OpCode
            val snapOp = db.collection("withdrawals").whereEqualTo("opCode", clean).limit(1).get().await()
            if (!snapOp.isEmpty) {
                return mapWithdrawalDoc(snapOp.documents.first())
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "findWithdrawalByCodeOrPin error: ${e.message}")
            null
        }
    }

    private fun mapWithdrawalDoc(doc: DocumentSnapshot): CloudWithdrawal {
        return CloudWithdrawal(
            id = doc.getStringSafe("id", doc.id),
            uid = doc.getStringSafe("uid", ""),
            userDni = doc.getStringSafe("userDni", ""),
            userPhone = doc.getStringSafe("userPhone", ""),
            accountHolder = doc.getStringSafe("accountHolder", "Usuario BC-BANK"),
            amount = doc.getDoubleSafe("amount", 0.0),
            pinCode = doc.getStringSafe("pinCode", ""),
            opCode = doc.getStringSafe("opCode", ""),
            qrData = doc.getStringSafe("qrData", ""),
            status = doc.getStringSafe("status", "PENDING"),
            createdAt = doc.getLongSafe("createdAt", System.currentTimeMillis()),
            expiresAt = doc.getLongSafe("expiresAt", System.currentTimeMillis() + 3600000L)
        )
    }

    // --- SERVICES ARCHITECTURE & CRUD ---

    fun getDefaultServices(): List<PublicService> {
        return emptyList()
    }

    private fun getLegacyFallbackServices(): List<PublicService> {
        return listOf(
            // 1. Luz y Electricidad
            PublicService(
                id = "srv_luz_enel",
                name = "Enel Distribución Perú",
                category = "Luz y Electricidad",
                code = "ENEL-01",
                commission = 0.0,
                description = "Pago de recibos de luz para Lima Norte, Centro y Callao",
                supplyCodeLabel = "N° de Cliente / Suministro (8 dígitos)",
                supplyCodePlaceholder = "Ej: 18492045",
                supplyCodeMinLength = 6,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_luz_delsur",
                name = "Luz del Sur",
                category = "Luz y Electricidad",
                code = "LDS-01",
                commission = 0.0,
                description = "Suministro eléctrico para Lima Sur y Este",
                supplyCodeLabel = "N° de Suministro (7 dígitos)",
                supplyCodePlaceholder = "Ej: 2948192",
                supplyCodeMinLength = 6,
                active = true,
                priority = 2
            ),
            PublicService(
                id = "srv_luz_hidrandina",
                name = "Hidrandina",
                category = "Luz y Electricidad",
                code = "HIDR-01",
                commission = 0.0,
                description = "Energía eléctrica para La Libertad, Cajamarca y Ancash",
                supplyCodeLabel = "Código de Suministro",
                supplyCodePlaceholder = "Ej: 4819204",
                supplyCodeMinLength = 5,
                active = true,
                priority = 3
            ),
            PublicService(
                id = "srv_luz_electroperu",
                name = "Electro Oriente / Electro Sur",
                category = "Luz y Electricidad",
                code = "ELEC-01",
                commission = 0.0,
                description = "Distribuidora de energía de macro-regiones",
                supplyCodeLabel = "N° de Suministro",
                supplyCodePlaceholder = "Ej: 3918204",
                supplyCodeMinLength = 5,
                active = true,
                priority = 4
            ),

            // 2. Agua y Alcantarillado
            PublicService(
                id = "srv_agua_sedapal",
                name = "Sedapal",
                category = "Agua Potable",
                code = "SED-01",
                commission = 0.0,
                description = "Servicio de agua potable y alcantarillado de Lima y Callao",
                supplyCodeLabel = "N° de Suministro / Cuenta (7 dígitos)",
                supplyCodePlaceholder = "Ej: 7492819",
                supplyCodeMinLength = 6,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_agua_sedalib",
                name = "Sedalib Trujillo",
                category = "Agua Potable",
                code = "SEDL-01",
                commission = 0.0,
                description = "Servicio de agua potable en La Libertad",
                supplyCodeLabel = "Código de Inscripción",
                supplyCodePlaceholder = "Ej: 5829104",
                supplyCodeMinLength = 5,
                active = true,
                priority = 2
            ),
            PublicService(
                id = "srv_agua_sedapar",
                name = "Sedapar Arequipa",
                category = "Agua Potable",
                code = "SEDP-01",
                commission = 0.0,
                description = "Servicio de saneamiento y agua en Arequipa",
                supplyCodeLabel = "N° de Suministro",
                supplyCodePlaceholder = "Ej: 6928103",
                supplyCodeMinLength = 5,
                active = true,
                priority = 3
            ),

            // 3. Gas Natural
            PublicService(
                id = "srv_gas_calidda",
                name = "Cálidda Gas Natural",
                category = "Gas Natural",
                code = "CAL-01",
                commission = 0.0,
                description = "Gas natural residencial y comercial en Lima y Callao",
                supplyCodeLabel = "N° de Cuenta de Contrato (8 dígitos)",
                supplyCodePlaceholder = "Ej: 38291049",
                supplyCodeMinLength = 6,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_gas_quavii",
                name = "Quavii Gas Natural Norte",
                category = "Gas Natural",
                code = "QUA-01",
                commission = 0.0,
                description = "Distribución de gas natural en el norte del Perú",
                supplyCodeLabel = "N° de Suministro / Contrato",
                supplyCodePlaceholder = "Ej: 4920194",
                supplyCodeMinLength = 5,
                active = true,
                priority = 2
            ),

            // 4. Telecomunicaciones & Internet
            PublicService(
                id = "srv_telco_movistar_hogar",
                name = "Movistar Hogar (Trío / Dúo / Fibra)",
                category = "Telecomunicaciones & Internet",
                code = "MOV-HOG",
                commission = 0.0,
                description = "Internet fibra óptica, telefonía fija y televisión",
                supplyCodeLabel = "N° de Teléfono Fijo o Código de Cliente",
                supplyCodePlaceholder = "Ej: 014567890 o 987654321",
                supplyCodeMinLength = 6,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_telco_claro_hogar",
                name = "Claro Hogar & Fibra Óptica",
                category = "Telecomunicaciones & Internet",
                code = "CLA-HOG",
                commission = 0.0,
                description = "Internet residencial, Claro TV y telefonía fija",
                supplyCodeLabel = "Código de Cliente / N° de Recibo",
                supplyCodePlaceholder = "Ej: 83920194",
                supplyCodeMinLength = 6,
                active = true,
                priority = 2
            ),
            PublicService(
                id = "srv_telco_win",
                name = "WIN Internet 100% Fibra",
                category = "Telecomunicaciones & Internet",
                code = "WIN-01",
                commission = 0.0,
                description = "Internet de fibra óptica residencial simétrico",
                supplyCodeLabel = "Código de Contrato / DNI Titular",
                supplyCodePlaceholder = "Ej: 72819283",
                supplyCodeMinLength = 6,
                active = true,
                priority = 3
            ),
            PublicService(
                id = "srv_telco_wow",
                name = "WOW / Nubyx Fibra",
                category = "Telecomunicaciones & Internet",
                code = "WOW-01",
                commission = 0.0,
                description = "Internet fibra óptica en provincias y Lima",
                supplyCodeLabel = "Código de Abonado",
                supplyCodePlaceholder = "Ej: 4920194",
                supplyCodeMinLength = 5,
                active = true,
                priority = 4
            ),

            // 5. Telefonía Móvil (Pospago y Prepago)
            PublicService(
                id = "srv_movil_movistar",
                name = "Movistar Móvil",
                category = "Telefonía Móvil",
                code = "MOV-CEL",
                commission = 0.0,
                description = "Recargas y pago de recibo mensual pospago",
                supplyCodeLabel = "N° de Celular (9 dígitos)",
                supplyCodePlaceholder = "Ej: 987654321",
                supplyCodeMinLength = 9,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_movil_claro",
                name = "Claro Móvil",
                category = "Telefonía Móvil",
                code = "CLA-CEL",
                commission = 0.0,
                description = "Planes Max y recargas prepago",
                supplyCodeLabel = "N° de Celular (9 dígitos)",
                supplyCodePlaceholder = "Ej: 912345678",
                supplyCodeMinLength = 9,
                active = true,
                priority = 2
            ),
            PublicService(
                id = "srv_movil_entel",
                name = "Entel Perú Móvil",
                category = "Telefonía Móvil",
                code = "ENT-CEL",
                commission = 0.0,
                description = "Pago de líneas pospago y recargas Entel",
                supplyCodeLabel = "N° de Celular (9 dígitos)",
                supplyCodePlaceholder = "Ej: 998765432",
                supplyCodeMinLength = 9,
                active = true,
                priority = 3
            ),
            PublicService(
                id = "srv_movil_bitel",
                name = "Bitel Perú Móvil",
                category = "Telefonía Móvil",
                code = "BIT-CEL",
                commission = 0.0,
                description = "Planes ilimitados y recargas prepago Bitel",
                supplyCodeLabel = "N° de Celular (9 dígitos)",
                supplyCodePlaceholder = "Ej: 934567890",
                supplyCodeMinLength = 9,
                active = true,
                priority = 4
            ),

            // 6. Impuestos y Municipalidad
            PublicService(
                id = "srv_sat_lima",
                name = "SAT Lima - Arbitrios y Papeletas",
                category = "Impuestos y Municipalidad",
                code = "SAT-LIM",
                commission = 0.0,
                description = "Impuesto vehicular, predial, arbitrios y papeletas",
                supplyCodeLabel = "Código de Contribuyente o Placa",
                supplyCodePlaceholder = "Ej: 194820 o ABC-123",
                supplyCodeMinLength = 4,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_sunat_tributos",
                name = "SUNAT - Tributos y Boletas NPS",
                category = "Impuestos y Municipalidad",
                code = "SUN-NPS",
                commission = 0.0,
                description = "Número de Pago SUNAT (NPS), IGV y Renta",
                supplyCodeLabel = "N° de Pago SUNAT (NPS) o RUC",
                supplyCodePlaceholder = "Ej: 98201928301",
                supplyCodeMinLength = 8,
                active = true,
                priority = 2
            ),

            // 7. Educación
            PublicService(
                id = "srv_educ_pucp",
                name = "Pontificia Universidad Católica (PUCP)",
                category = "Educación",
                code = "PUCP-01",
                commission = 0.0,
                description = "Pensiones de pregrado y posgrado",
                supplyCodeLabel = "Código de Alumno (8 dígitos)",
                supplyCodePlaceholder = "Ej: 20241928",
                supplyCodeMinLength = 8,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_educ_upc",
                name = "Universidad Peruana de Ciencias Aplicadas (UPC)",
                category = "Educación",
                code = "UPC-01",
                commission = 0.0,
                description = "Pensiones académicas y trámites",
                supplyCodeLabel = "Código de Alumno (U202XXXXX)",
                supplyCodePlaceholder = "Ej: U20241829",
                supplyCodeMinLength = 8,
                active = true,
                priority = 2
            ),
            PublicService(
                id = "srv_educ_unmsm",
                name = "Universidad Nacional Mayor de San Marcos",
                category = "Educación",
                code = "UNMSM-01",
                commission = 0.0,
                description = "Derechos de examen, matrículas y certificados",
                supplyCodeLabel = "Código de Postulante / Matrícula",
                supplyCodePlaceholder = "Ej: 24190821",
                supplyCodeMinLength = 6,
                active = true,
                priority = 3
            ),
            PublicService(
                id = "srv_educ_senati",
                name = "SENATI",
                category = "Educación",
                code = "SEN-01",
                commission = 0.0,
                description = "Pensiones y matrícula de carreras técnicas",
                supplyCodeLabel = "ID de Alumno SENATI",
                supplyCodePlaceholder = "Ej: 001294819",
                supplyCodeMinLength = 6,
                active = true,
                priority = 4
            ),

            // 8. Tarjetas y Financiero
            PublicService(
                id = "srv_fin_ripley",
                name = "Banco Ripley / Tarjeta Ripley",
                category = "Tarjetas y Financiero",
                code = "RIP-01",
                commission = 0.0,
                description = "Pago de estado de cuenta y tarjetas de crédito",
                supplyCodeLabel = "N° de Tarjeta o DNI Titular",
                supplyCodePlaceholder = "Ej: 72819283",
                supplyCodeMinLength = 8,
                active = true,
                priority = 1
            ),
            PublicService(
                id = "srv_fin_cmr",
                name = "CMR Falabella",
                category = "Tarjetas y Financiero",
                code = "CMR-01",
                commission = 0.0,
                description = "Pago de tarjeta CMR Visa / Mastercard",
                supplyCodeLabel = "N° de Tarjeta o DNI Titular",
                supplyCodePlaceholder = "Ej: 72819283",
                supplyCodeMinLength = 8,
                active = true,
                priority = 2
            ),
            PublicService(
                id = "srv_fin_oh",
                name = "Tarjeta Oh! / Financiera Oh",
                category = "Tarjetas y Financiero",
                code = "TOH-01",
                commission = 0.0,
                description = "Pago mensual de compras y créditos",
                supplyCodeLabel = "N° de Tarjeta Oh! o DNI",
                supplyCodePlaceholder = "Ej: 72819283",
                supplyCodeMinLength = 8,
                active = true,
                priority = 3
            ),
            PublicService(
                id = "srv_fin_cencosud",
                name = "Tarjeta Cencosud (Metro / Wong)",
                category = "Tarjetas y Financiero",
                code = "CENC-01",
                commission = 0.0,
                description = "Pago de compras en tiendas por departamento",
                supplyCodeLabel = "N° de Tarjeta o DNI",
                supplyCodePlaceholder = "Ej: 72819283",
                supplyCodeMinLength = 8,
                active = true,
                priority = 4
            )
        )
    }

    suspend fun getPublicServices(): List<PublicService> {
        if (!isFirebaseAvailable) return emptyList()
        return try {
            val db = FirebaseFirestore.getInstance()
            val snap = db.collection("services").get().await()
            val cloudServices = snap.documents.mapNotNull { doc ->
                val name = doc.getStringSafe("name", "").ifBlank { return@mapNotNull null }
                PublicService(
                    id = doc.id,
                    name = name,
                    category = doc.getStringSafe("category", "Servicios Públicos"),
                    code = doc.getStringSafe("code", ""),
                    fee = doc.getDoubleSafe("fee", 0.0),
                    commission = doc.getDoubleSafe("commission", 0.0),
                    description = doc.getStringSafe("description", ""),
                    supplyCodeLabel = doc.getStringSafe("supplyCodeLabel", "Código de Suministro / N° de Recibo"),
                    supplyCodePlaceholder = doc.getStringSafe("supplyCodePlaceholder", "Ej: 1849204"),
                    supplyCodeMinLength = doc.getLongSafe("supplyCodeMinLength", 4L).toInt(),
                    active = doc.getBooleanSafe("active", true),
                    priority = doc.getLongSafe("priority", 1L).toInt()
                )
            }
            cloudServices.sortedBy { it.priority }
        } catch (e: Exception) {
            Log.w(TAG, "getPublicServices from Firestore failed: ${e.message}")
            emptyList()
        }
    }

    fun listenToPublicServices(onServicesUpdated: (List<PublicService>) -> Unit): ListenerRegistration? {
        if (!isFirebaseAvailable) {
            onServicesUpdated(emptyList())
            return null
        }

        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("services").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onServicesUpdated(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getStringSafe("name", "").ifBlank { return@mapNotNull null }
                    PublicService(
                        id = doc.id,
                        name = name,
                        category = doc.getStringSafe("category", "Servicios Públicos"),
                        code = doc.getStringSafe("code", ""),
                        fee = doc.getDoubleSafe("fee", 0.0),
                        commission = doc.getDoubleSafe("commission", 0.0),
                        description = doc.getStringSafe("description", ""),
                        supplyCodeLabel = doc.getStringSafe("supplyCodeLabel", "Código de Suministro / N° de Recibo"),
                        supplyCodePlaceholder = doc.getStringSafe("supplyCodePlaceholder", "Ej: 1849204"),
                        supplyCodeMinLength = doc.getLongSafe("supplyCodeMinLength", 4L).toInt(),
                        active = doc.getBooleanSafe("active", true),
                        priority = doc.getLongSafe("priority", 1L).toInt()
                    )
                }.sortedBy { it.priority }

                onServicesUpdated(list)
            }
        } catch (e: Exception) {
            Log.w(TAG, "listenToPublicServices setup failed: ${e.message}")
            onServicesUpdated(emptyList())
            null
        }
    }

    suspend fun seedDefaultPublicServicesIfEmpty() {
        // Services are managed strictly via Firestore and Admin Panel
    }

    suspend fun savePublicService(service: PublicService): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "id" to service.id,
                "name" to service.name,
                "category" to service.category,
                "code" to service.code,
                "fee" to service.fee,
                "commission" to service.commission,
                "description" to service.description,
                "supplyCodeLabel" to service.supplyCodeLabel,
                "supplyCodePlaceholder" to service.supplyCodePlaceholder,
                "supplyCodeMinLength" to service.supplyCodeMinLength,
                "active" to service.active,
                "priority" to service.priority,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("services").document(service.id).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePublicService(serviceId: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("services").document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordServicePayment(payment: ServicePayment): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val db = FirebaseFirestore.getInstance()
            val now = payment.timestamp
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = if (payment.timestampFormatted.isNotBlank()) payment.timestampFormatted else dateFormatter.format(Date(now))

            val data = hashMapOf<String, Any>(
                "id" to payment.id,
                "uid" to payment.uid,
                "accountHolder" to payment.accountHolder,
                "userDni" to payment.userDni,
                "userPhone" to payment.userPhone,
                "serviceId" to payment.serviceId,
                "serviceName" to payment.serviceName,
                "serviceCategory" to payment.serviceCategory,
                "supplyCode" to payment.supplyCode,
                "amount" to payment.amount,
                "commission" to payment.commission,
                "totalPaid" to payment.totalPaid,
                "operationCode" to payment.operationCode,
                "comment" to payment.comment,
                "status" to payment.status,
                "timestamp" to now,
                "timestampFormatted" to formattedDate
            )

            // 1. Root collection /service_payments/{payment.id} for general reporting & admin reconciliations
            db.collection("service_payments").document(payment.id).set(data, SetOptions.merge()).await()

            // 2. User specific subcollection /users/{uid}/service_payments/{payment.id}
            if (payment.uid.isNotBlank() && payment.uid != "local_user") {
                db.collection("users").document(payment.uid)
                    .collection("service_payments").document(payment.id)
                    .set(data, SetOptions.merge()).await()
            }

            // 3. Security audit
            recordSecurityAuditLog(
                uid = payment.uid,
                eventType = "SERVICE_PAYMENT_PROCESSED",
                details = "Pago de servicio ${payment.serviceName} por S/ ${String.format(Locale.US, "%.2f", payment.totalPaid)} (Suministro: ${payment.supplyCode}, Op: ${payment.operationCode})",
                dni = payment.userDni
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "recordServicePayment error: ${e.message}")
            Result.failure(e)
        }
    }

    // --- SUPPORT CHANNELS (CONFIGURABLE COLLECTION) ---
    suspend fun seedDefaultSupportChannelsIfEmpty() {
        // Support channels are managed strictly via Firestore and Admin Panel
    }

    fun listenToSupportChannels(
        onChannelsChanged: (List<SupportChannel>) -> Unit
    ): ListenerRegistration? {
        if (!isFirebaseAvailable) {
            onChannelsChanged(emptyList())
            return null
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("support_channels").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    onChannelsChanged(emptyList())
                    return@addSnapshotListener
                }
                val channels = snapshot.documents.mapNotNull { doc ->
                    val type = doc.getStringSafe("type", "WHATSAPP").uppercase()
                    val title = doc.getStringSafe("title", "Canal de Soporte")
                    val value = doc.getStringSafe("value", "")
                    val isAvailable = if (doc.contains("isAvailable")) doc.getBooleanSafe("isAvailable", true) else doc.getBooleanSafe("available", true)
                    val isPrimary = if (doc.contains("isPrimary")) doc.getBooleanSafe("isPrimary", false) else doc.getBooleanSafe("primary", false)
                    val priority = doc.getLongSafe("priority", 10L).toInt()
                    val actionUrl = doc.getStringSafe("actionUrl", "")
                    val description = doc.getStringSafe("description", "")
                    val updatedAt = doc.getLongSafe("updatedAt", System.currentTimeMillis())

                    SupportChannel(
                        id = doc.id,
                        type = type,
                        title = title,
                        value = value,
                        description = description,
                        actionUrl = actionUrl,
                        isAvailable = isAvailable,
                        isPrimary = isPrimary,
                        priority = priority,
                        updatedAt = updatedAt
                    )
                }
                .filter { it.isAvailable }
                .sortedWith(compareByDescending<SupportChannel> { it.isPrimary }.thenBy { it.priority })

                onChannelsChanged(channels)
            }
        } catch (e: Exception) {
            Log.e(TAG, "listenToSupportChannels error: ${e.message}")
            onChannelsChanged(emptyList())
            null
        }
    }

    fun getDefaultLocalSupportChannels(): List<SupportChannel> {
        return emptyList()
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(email.trim()).await()
            recordSecurityAuditLog(
                uid = "PASSWORD_RESET",
                eventType = "PASSWORD_RESET_REQUESTED",
                details = "Enlace de restablecimiento enviado a $email",
                userEmail = email
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserEmail(uid: String, newEmail: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val authUser = FirebaseAuth.getInstance().currentUser
            if (authUser != null) {
                authUser.updateEmail(newEmail.trim()).await()
            }
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).set(
                hashMapOf(
                    "email" to newEmail.trim(),
                    "emailUpdatedTimestamp" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
            recordSecurityAuditLog(
                uid = uid,
                eventType = "EMAIL_SYNCHRONIZED",
                details = "Correo institucional/personal sincronizado con éxito a $newEmail",
                userEmail = newEmail
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateUserEmail error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateUserPasswordDirect(email: String, newPass: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.email.equals(email, ignoreCase = true)) {
                user.updatePassword(newPass).await()
            }
            recordSecurityAuditLog(
                uid = user?.uid ?: "PASSWORD_CHANGE",
                eventType = "PASSWORD_RESET_COMPLETED",
                details = "Clave de acceso actualizada exitosamente para $email",
                userEmail = email
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "updateUserPasswordDirect error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun syncAccountToFirestore(account: AccountInfoEntity, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            val accountData = hashMapOf(
                "accountHolder" to account.accountHolder,
                "bankName" to account.bankName,
                "accountNumber" to account.accountNumber,
                "cciNumber" to account.cciNumber,
                "cardLastFour" to account.cardLastFour,
                "balance" to account.balance,
                "lastSync" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).collection("account").document("main")
                .set(accountData, SetOptions.merge()).await()
            Log.d(TAG, "Account synced to Firestore for user: $uid")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore account sync notice: ${e.message}")
        }
    }

    suspend fun syncTransactionToFirestore(transaction: TransactionEntity, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            val txData = hashMapOf(
                "id" to transaction.id,
                "title" to transaction.title,
                "amount" to transaction.amount,
                "type" to transaction.type,
                "category" to transaction.category,
                "timestamp" to transaction.timestamp,
                "recipientOrSender" to transaction.recipientOrSender,
                "referenceNumber" to transaction.referenceNumber,
                "note" to transaction.note
            )
            val docId = transaction.referenceNumber.ifEmpty { "${transaction.id}" }
            db.collection("users").document(uid).collection("transactions").document(docId)
                .set(txData, SetOptions.merge()).await()
            Log.d(TAG, "Transaction synced to Firestore: $docId")
        } catch (e: Exception) {
            Log.w(TAG, "Firestore tx sync notice: ${e.message}")
        }
    }

    suspend fun syncSavingsGoalToFirestore(goal: SavingsGoalEntity, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            val goalData = hashMapOf(
                "id" to goal.id,
                "name" to goal.name,
                "targetAmount" to goal.targetAmount,
                "currentAmount" to goal.currentAmount,
                "categoryIcon" to goal.categoryIcon,
                "targetDate" to goal.targetDate,
                "colorHex" to goal.colorHex,
                "status" to goal.status
            )
            db.collection("users").document(uid).collection("savings_goals").document("${goal.id}")
                .set(goalData, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore goal sync notice: ${e.message}")
        }
    }

    suspend fun deleteSavingsGoalFromFirestore(goalId: Long, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).collection("savings_goals").document("$goalId")
                .delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore goal delete notice: ${e.message}")
        }
    }

    suspend fun syncBudgetToFirestore(budget: BudgetEntity, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            val budgetData = hashMapOf(
                "id" to budget.id,
                "category" to budget.category,
                "monthlyLimit" to budget.monthlyLimit,
                "spentAmount" to budget.spentAmount,
                "iconName" to budget.iconName
            )
            db.collection("users").document(uid).collection("budgets").document("${budget.id}")
                .set(budgetData, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore budget sync notice: ${e.message}")
        }
    }

    suspend fun deleteBudgetFromFirestore(budgetId: Long, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).collection("budgets").document("$budgetId")
                .delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore budget delete notice: ${e.message}")
        }
    }

    suspend fun syncNotificationToFirestore(notification: BankNotificationEntity, uid: String) {
        if (!isFirebaseAvailable) return
        try {
            val db = FirebaseFirestore.getInstance()
            val notifData = hashMapOf(
                "id" to notification.id,
                "uid" to uid,
                "title" to notification.title,
                "message" to notification.message,
                "category" to notification.category,
                "timestamp" to notification.timestamp,
                "isRead" to notification.isRead,
                "amountTag" to (notification.amountTag ?: ""),
                "type" to notification.type,
                "status" to if (notification.isRead) "SEEN" else "UNSEEN",
                "deleted" to false
            )
            val docId = if (notification.id > 0) "${notification.id}" else "${System.currentTimeMillis()}"
            db.collection("users").document(uid).collection("notifications").document(docId)
                .set(notifData, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore notification sync notice: ${e.message}")
        }
    }

    suspend fun updateNotificationStatusInFirestore(uid: String, notificationId: Long, status: String) {
        if (!isFirebaseAvailable || uid.isBlank()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val notifsCol = db.collection("users").document(uid).collection("notifications")
            val isRead = status.equals("SEEN", ignoreCase = true)
            val isDeleted = status.equals("DELETED", ignoreCase = true)
            val updateMap = hashMapOf<String, Any>(
                "status" to status,
                "isRead" to isRead,
                "deleted" to isDeleted
            )
            val docRef = notifsCol.document("$notificationId")
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                docRef.update(updateMap).await()
            } else {
                val query = notifsCol.whereEqualTo("id", notificationId).limit(1).get().await()
                if (!query.isEmpty) {
                    query.documents.first().reference.update(updateMap).await()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "updateNotificationStatusInFirestore notice: ${e.message}")
        }
    }

    suspend fun markAllNotificationsAsSeenInFirestore(uid: String) {
        if (!isFirebaseAvailable || uid.isBlank()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users").document(uid).collection("notifications").get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, mapOf(
                    "status" to "SEEN",
                    "isRead" to true
                ))
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.w(TAG, "markAllNotificationsAsSeenInFirestore notice: ${e.message}")
        }
    }

    suspend fun clearAllNotificationsInFirestore(uid: String) {
        if (!isFirebaseAvailable || uid.isBlank()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users").document(uid).collection("notifications").get().await()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, mapOf(
                    "status" to "DELETED",
                    "deleted" to true
                ))
            }
            batch.commit().await()
        } catch (e: Exception) {
            Log.w(TAG, "clearAllNotificationsInFirestore notice: ${e.message}")
        }
    }

    suspend fun lookupRecipientByIdentifier(query: String): RecipientLookupResult? {
        val clean = query.trim()
        if (clean.isBlank()) return null
        if (!isFirebaseAvailable) return null

        return try {
            val db = FirebaseFirestore.getInstance()
            val digitsOnly = clean.filter { it.isDigit() }

            // 1. Search in users collection by DNI (8 digits)
            if (digitsOnly.length == 8) {
                val userByDni = db.collection("users").whereEqualTo("dni", digitsOnly).limit(1).get().await()
                if (!userByDni.isEmpty) {
                    val doc = userByDni.documents.first()
                    val name = doc.getStringSafe("fullName", doc.getStringSafe("accountHolder", "Usuario BC-BANK"))
                    val bank = doc.getStringSafe("bankName", "BC-BANK Perú")
                    return RecipientLookupResult(fullName = name, identifier = clean, bankName = bank, found = true)
                }
            }

            // 2. Search in users collection by accountNumber
            val userByAcc = db.collection("users").whereEqualTo("accountNumber", clean).limit(1).get().await()
            if (!userByAcc.isEmpty) {
                val doc = userByAcc.documents.first()
                val name = doc.getStringSafe("fullName", doc.getStringSafe("accountHolder", "Usuario BC-BANK"))
                val bank = doc.getStringSafe("bankName", "BC-BANK Perú")
                return RecipientLookupResult(fullName = name, identifier = clean, bankName = bank, found = true)
            }

            // 3. Search in users collection by cciNumber
            val userByCci = db.collection("users").whereEqualTo("cciNumber", clean).limit(1).get().await()
            if (!userByCci.isEmpty) {
                val doc = userByCci.documents.first()
                val name = doc.getStringSafe("fullName", doc.getStringSafe("accountHolder", "Usuario BC-BANK"))
                val bank = doc.getStringSafe("bankName", "BC-BANK Perú")
                return RecipientLookupResult(fullName = name, identifier = clean, bankName = bank, found = true)
            }

            // 4. Search in cip_codes collection by DNI, accountNumber or cciNumber
            if (digitsOnly.length == 8) {
                val cipQuery = db.collection("cip_codes").whereEqualTo("userDni", digitsOnly).limit(1).get().await()
                if (!cipQuery.isEmpty) {
                    val doc = cipQuery.documents.first()
                    val name = doc.getStringSafe("fullName", doc.getStringSafe("accountHolder", "Usuario BC-BANK"))
                    val bank = doc.getStringSafe("bankName", "BC-BANK Perú")
                    return RecipientLookupResult(fullName = name, identifier = clean, bankName = bank, found = true)
                }
            }

            val cipAccQuery = db.collection("cip_codes").whereEqualTo("accountNumber", clean).limit(1).get().await()
            if (!cipAccQuery.isEmpty) {
                val doc = cipAccQuery.documents.first()
                val name = doc.getStringSafe("fullName", doc.getStringSafe("accountHolder", "Usuario BC-BANK"))
                val bank = doc.getStringSafe("bankName", "BC-BANK Perú")
                return RecipientLookupResult(fullName = name, identifier = clean, bankName = bank, found = true)
            }

            val cipCciQuery = db.collection("cip_codes").whereEqualTo("cciNumber", clean).limit(1).get().await()
            if (!cipCciQuery.isEmpty) {
                val doc = cipCciQuery.documents.first()
                val name = doc.getStringSafe("fullName", doc.getStringSafe("accountHolder", "Usuario BC-BANK"))
                val bank = doc.getStringSafe("bankName", "BC-BANK Perú")
                return RecipientLookupResult(fullName = name, identifier = clean, bankName = bank, found = true)
            }

            null
        } catch (e: Exception) {
            Log.w(TAG, "lookupRecipientByIdentifier notice: ${e.message}")
            null
        }
    }

    suspend fun syncCategoryToFirestore(
        uid: String,
        id: String,
        name: String,
        description: String,
        iconKey: String,
        type: String,
        colorHex: String = "#10B981"
    ) {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return
        try {
            val db = FirebaseFirestore.getInstance()
            val data = hashMapOf(
                "id" to id,
                "name" to name,
                "description" to description,
                "iconKey" to iconKey,
                "type" to type,
                "colorHex" to colorHex,
                "isCustom" to true,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).collection("categories").document(id)
                .set(data, SetOptions.merge()).await()
            Log.d(TAG, "Category $id synced to Firestore for user $uid")
        } catch (e: Exception) {
            Log.w(TAG, "syncCategoryToFirestore notice: ${e.message}")
        }
    }

    suspend fun deleteCategoryFromFirestore(uid: String, categoryId: String) {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return
        try {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(uid).collection("categories").document(categoryId)
                .delete().await()
            Log.d(TAG, "Category $categoryId deleted from Firestore for user $uid")
        } catch (e: Exception) {
            Log.w(TAG, "deleteCategoryFromFirestore notice: ${e.message}")
        }
    }

    fun listenToUserIncomingEvents(
        uid: String,
        onAccountBalanceChanged: (Double) -> Unit,
        onProfileChanged: ((name: String, phone: String, dni: String, accountType: String, isBusiness: Boolean, businessName: String, businessRuc: String) -> Unit)? = null,
        onAccountDetailsChanged: ((AccountInfoEntity) -> Unit)? = null,
        onSavingsGoalsChanged: ((List<SavingsGoalEntity>) -> Unit)? = null,
        onBudgetsChanged: ((List<BudgetEntity>) -> Unit)? = null,
        onTransactionsChanged: ((List<TransactionEntity>) -> Unit)? = null,
        onNotificationsChanged: ((List<BankNotificationEntity>) -> Unit)? = null
    ): List<ListenerRegistration> {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return emptyList()
        val listeners = mutableListOf<ListenerRegistration>()
        try {
            val db = FirebaseFirestore.getInstance()

            // 1. Account balance and details listener (from subcollection account/main)
            val accListener = db.collection("users").document(uid).collection("account").document("main")
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val balance = snapshot.getDoubleSafe("balance", 0.0)
                    onAccountBalanceChanged(balance)
                    if (onAccountDetailsChanged != null) {
                        val accEntity = AccountInfoEntity(
                            id = 1,
                            accountHolder = snapshot.getStringSafe("accountHolder", ""),
                            bankName = snapshot.getStringSafe("bankName", "Cuenta de Ahorros BC-BANK"),
                            accountNumber = snapshot.getStringSafe("accountNumber", ""),
                            cciNumber = snapshot.getStringSafe("cciNumber", ""),
                            cardLastFour = snapshot.getStringSafe("cardLastFour", ""),
                            balance = balance
                        )
                        onAccountDetailsChanged(accEntity)
                    }
                }
            listeners.add(accListener)

            // 2. User profile listener & root balance listener
            val profListener = db.collection("users").document(uid)
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                    val name = snapshot.getStringSafe("fullName", "")
                    val phone = snapshot.getStringSafe("phone", "")
                    val dni = snapshot.getStringSafe("dni", "")
                    val accountType = snapshot.getStringSafe("accountType", "Cuenta de Ahorros BC-BANK")
                    val isBusiness = snapshot.getBooleanSafe("isBusiness", false)
                    val bName = snapshot.getStringSafe("businessName", "")
                    val bRuc = snapshot.getStringSafe("businessRuc", "")
                    if (dni.isNotBlank() || name.isNotBlank()) {
                        onProfileChanged?.invoke(name, phone, dni, accountType, isBusiness, bName, bRuc)
                    }
                    if (snapshot.contains("balance")) {
                        val rootBalance = snapshot.getDoubleSafe("balance", -1.0)
                        if (rootBalance >= 0.0) {
                            onAccountBalanceChanged(rootBalance)
                        }
                    }
                }
            listeners.add(profListener)

            // 3. Savings Goals listener
            if (onSavingsGoalsChanged != null) {
                val goalsListener = db.collection("users").document(uid).collection("savings_goals")
                    .addSnapshotListener { snapshot, err ->
                        if (err != null || snapshot == null) return@addSnapshotListener
                        val goals = snapshot.documents.mapNotNull { doc ->
                            val name = doc.getStringSafe("name", "").ifBlank { return@mapNotNull null }
                            val target = doc.getDoubleSafe("targetAmount", 0.0)
                            val current = doc.getDoubleSafe("currentAmount", 0.0)
                            val st = doc.getStringSafe("status", if (current >= target && target > 0) "COMPLETED" else "IN_PROGRESS")
                            val rawId = doc.getLongSafe("id", -1L)
                            val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                            SavingsGoalEntity(
                                id = finalId,
                                name = name,
                                targetAmount = target,
                                currentAmount = current,
                                categoryIcon = doc.getStringSafe("categoryIcon", "OTHER"),
                                targetDate = doc.getStringSafe("targetDate", ""),
                                colorHex = doc.getStringSafe("colorHex", "#10B981"),
                                status = st
                            )
                        }
                        onSavingsGoalsChanged(goals)
                    }
                listeners.add(goalsListener)
            }

            // 4. Budgets listener
            if (onBudgetsChanged != null) {
                val budgetsListener = db.collection("users").document(uid).collection("budgets")
                    .addSnapshotListener { snapshot, err ->
                        if (err != null || snapshot == null) return@addSnapshotListener
                        val budgets = snapshot.documents.mapNotNull { doc ->
                            val cat = doc.getStringSafe("category", "").ifBlank { return@mapNotNull null }
                            val rawId = doc.getLongSafe("id", -1L)
                            val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                            BudgetEntity(
                                id = finalId,
                                category = cat,
                                monthlyLimit = doc.getDoubleSafe("monthlyLimit", 0.0),
                                spentAmount = doc.getDoubleSafe("spentAmount", 0.0),
                                iconName = doc.getStringSafe("iconName", "OTHER")
                            )
                        }
                        onBudgetsChanged(budgets)
                    }
                listeners.add(budgetsListener)
            }

            // 5. Transactions listener
            if (onTransactionsChanged != null) {
                val txListener = db.collection("users").document(uid).collection("transactions")
                    .addSnapshotListener { snapshot, err ->
                        if (err != null || snapshot == null) return@addSnapshotListener
                        val txs = snapshot.documents.mapNotNull { doc ->
                            val title = doc.getStringSafe("title", doc.getStringSafe("concept", "")).ifBlank { return@mapNotNull null }
                            val rawId = doc.getLongSafe("id", -1L)
                            val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                            TransactionEntity(
                                id = finalId,
                                title = title,
                                amount = doc.getDoubleSafe("amount", 0.0),
                                type = doc.getStringSafe("type", "EXPENSE"),
                                category = doc.getStringSafe("category", "General"),
                                timestamp = doc.getLongSafe("timestamp", doc.getLongSafe("date", System.currentTimeMillis())),
                                recipientOrSender = doc.getStringSafe("recipientOrSender", doc.getStringSafe("recipient", "")),
                                referenceNumber = doc.getStringSafe("referenceNumber", doc.id),
                                note = doc.getStringSafe("note", "")
                            )
                        }
                        onTransactionsChanged(txs)
                    }
                listeners.add(txListener)
            }

            // 6. Notifications listener (with UNSEEN, SEEN, DELETED state sync)
            if (onNotificationsChanged != null) {
                val notifListener = db.collection("users").document(uid).collection("notifications")
                    .addSnapshotListener { snapshot, err ->
                        if (err != null || snapshot == null) return@addSnapshotListener
                        val notifs = snapshot.documents.mapNotNull { doc ->
                            val title = doc.getStringSafe("title", "").ifBlank { return@mapNotNull null }
                            val status = doc.getStringSafe("status", "UNSEEN")
                            val isDeleted = doc.getBooleanSafe("deleted", false) || status.equals("DELETED", ignoreCase = true)
                            if (isDeleted) return@mapNotNull null

                            val rawId = doc.getLongSafe("id", -1L)
                            val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                            val isRead = doc.getBooleanSafe("isRead", false) || status.equals("SEEN", ignoreCase = true)

                            BankNotificationEntity(
                                id = finalId,
                                uid = uid,
                                title = title,
                                message = doc.getStringSafe("message", ""),
                                category = doc.getStringSafe("category", "Seguridad"),
                                timestamp = doc.getLongSafe("timestamp", System.currentTimeMillis()),
                                isRead = isRead,
                                amountTag = doc.getStringSafe("amountTag", "").ifBlank { null },
                                type = doc.getStringSafe("type", "GENERAL")
                            )
                        }
                        onNotificationsChanged(notifs)
                    }
                listeners.add(notifListener)
            }

            // 7. Categories listener (Loads strictly user-created categories from Firestore)
            val catListener = db.collection("users").document(uid).collection("categories")
                .addSnapshotListener { snapshot, err ->
                    if (err != null || snapshot == null) return@addSnapshotListener
                    val expList = mutableListOf<com.example.ui.util.CategoryItem>()
                    val incList = mutableListOf<com.example.ui.util.CategoryItem>()
                    val goalList = mutableListOf<com.example.ui.util.CategoryItem>()
                    val budList = mutableListOf<com.example.ui.util.CategoryItem>()

                    snapshot.documents.forEach { doc ->
                        val name = doc.getStringSafe("name", "").trim()
                        if (name.isNotBlank()) {
                            val type = doc.getStringSafe("type", "EXPENSE").uppercase()
                            val iconKey = doc.getStringSafe("iconKey", "DEFAULT")
                            val item = com.example.ui.util.CategoryItem(
                                id = doc.id,
                                name = name,
                                isCustom = true,
                                icon = iconKey
                            )
                            when (type) {
                                "EXPENSE" -> expList.add(item)
                                "INCOME" -> incList.add(item)
                                "GOAL" -> goalList.add(item)
                                "BUDGET" -> budList.add(item)
                            }
                        }
                    }

                    com.example.ui.util.CustomCategoryManager.setCategoriesFromCloud(expList, com.example.ui.util.CategoryType.EXPENSE)
                    com.example.ui.util.CustomCategoryManager.setCategoriesFromCloud(incList, com.example.ui.util.CategoryType.INCOME)
                    com.example.ui.util.CustomCategoryManager.setCategoriesFromCloud(goalList, com.example.ui.util.CategoryType.GOAL)
                    com.example.ui.util.CustomCategoryManager.setCategoriesFromCloud(budList, com.example.ui.util.CategoryType.BUDGET)
                }
            listeners.add(catListener)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach real-time listeners: ${e.message}")
        }
        return listeners
    }


    suspend fun loadUserDataFromFirestore(uid: String): UserCloudData? {
        if (!isFirebaseAvailable || uid.isBlank() || uid == "local_user") return null
        return try {
            val db = FirebaseFirestore.getInstance()
            
            // Get user profile
            val userDoc = db.collection("users").document(uid).get().await()
            val fullName = userDoc.getStringSafe("fullName", "Usuario BC-BANK")
            val dni = userDoc.getStringSafe("dni", "")
            val phone = userDoc.getStringSafe("phone", "")
            val email = userDoc.getStringSafe("email", FirebaseAuth.getInstance().currentUser?.email ?: "")
            val accountType = userDoc.getStringSafe("accountType", "Cuenta de Ahorros BC-BANK")
            val isBusiness = userDoc.getBooleanSafe("isBusiness", false)
            val businessName = userDoc.getStringSafe("businessName", "")
            val businessRuc = userDoc.getStringSafe("businessRuc", "")
            val profileComplete = userDoc.getBooleanSafe("profileComplete", dni.isNotBlank() && fullName.isNotBlank() && fullName != "Usuario BC-BANK")

            // Get account (try subcollection, then fallback to userDoc root)
            val accountDoc = try {
                db.collection("users").document(uid).collection("account").document("main").get().await()
            } catch (e: Exception) { null }

            val account = if (accountDoc != null && accountDoc.exists()) {
                AccountInfoEntity(
                    id = 1,
                    accountHolder = accountDoc.getStringSafe("accountHolder", fullName),
                    bankName = accountDoc.getStringSafe("bankName", accountType),
                    accountNumber = accountDoc.getStringSafe("accountNumber", ""),
                    cciNumber = accountDoc.getStringSafe("cciNumber", ""),
                    cardLastFour = accountDoc.getStringSafe("cardLastFour", ""),
                    balance = accountDoc.getDoubleSafe("balance", 0.0)
                )
            } else {
                // Fallback to userDoc root fields or deterministic account
                val balanceFromUser = userDoc.getDoubleSafe("balance", userDoc.getDoubleSafe("initialBalance", 0.0))
                val dniClean = dni.filter { it.isDigit() }.padStart(8, '0').takeLast(8)
                val accMid = (10000000L + (dniClean.toLongOrNull() ?: 12345678L) * 7L % 89999999L).toString()
                val accNum = userDoc.getStringSafe("accountNumber", "194-$accMid-0-88")
                val cciNum = userDoc.getStringSafe("cciNumber", "002-194-00${dniClean.toLongOrNull() ?: 12345678L}-42")
                val cardLast4 = userDoc.getStringSafe("cardLastFour", dniClean.takeLast(4).ifBlank { "8888" })
                val newAcc = AccountInfoEntity(
                    id = 1,
                    accountHolder = userDoc.getStringSafe("accountHolder", fullName),
                    bankName = userDoc.getStringSafe("bankName", accountType),
                    accountNumber = accNum,
                    cciNumber = cciNum,
                    cardLastFour = cardLast4,
                    balance = balanceFromUser
                )
                // Persist it into subcollection for next accesses
                try {
                    syncAccountToFirestore(newAcc, uid)
                } catch (_: Exception) {}
                newAcc
            }

            // Get transactions
            val txSnap = try {
                db.collection("users").document(uid).collection("transactions").get().await()
            } catch (e: Exception) { null }
            val transactions = txSnap?.documents?.mapNotNull { doc ->
                val title = doc.getStringSafe("title", doc.getStringSafe("concept", "")).ifBlank { return@mapNotNull null }
                val rawId = doc.getLongSafe("id", -1L)
                val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                TransactionEntity(
                    id = finalId,
                    title = title,
                    amount = doc.getDoubleSafe("amount", 0.0),
                    type = doc.getStringSafe("type", "EXPENSE"),
                    category = doc.getStringSafe("category", "General"),
                    timestamp = doc.getLongSafe("timestamp", doc.getLongSafe("date", System.currentTimeMillis())),
                    recipientOrSender = doc.getStringSafe("recipientOrSender", doc.getStringSafe("recipient", "")),
                    referenceNumber = doc.getStringSafe("referenceNumber", doc.id),
                    note = doc.getStringSafe("note", "")
                )
            } ?: emptyList()

            // Get goals
            val goalsSnap = try {
                db.collection("users").document(uid).collection("savings_goals").get().await()
            } catch (e: Exception) { null }
            val goals = goalsSnap?.documents?.mapNotNull { doc ->
                val name = doc.getStringSafe("name", "").ifBlank { return@mapNotNull null }
                val target = doc.getDoubleSafe("targetAmount", 0.0)
                val current = doc.getDoubleSafe("currentAmount", 0.0)
                val st = doc.getStringSafe("status", if (current >= target && target > 0) "COMPLETED" else "IN_PROGRESS")
                val rawId = doc.getLongSafe("id", -1L)
                val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                SavingsGoalEntity(
                    id = finalId,
                    name = name,
                    targetAmount = target,
                    currentAmount = current,
                    categoryIcon = doc.getStringSafe("categoryIcon", "OTHER"),
                    targetDate = doc.getStringSafe("targetDate", ""),
                    colorHex = doc.getStringSafe("colorHex", "#10B981"),
                    status = st
                )
            } ?: emptyList()

            // Get budgets
            val budgetsSnap = try {
                db.collection("users").document(uid).collection("budgets").get().await()
            } catch (e: Exception) { null }
            val budgets = budgetsSnap?.documents?.mapNotNull { doc ->
                val cat = doc.getStringSafe("category", "").ifBlank { return@mapNotNull null }
                val rawId = doc.getLongSafe("id", -1L)
                val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                BudgetEntity(
                    id = finalId,
                    category = cat,
                    monthlyLimit = doc.getDoubleSafe("monthlyLimit", 0.0),
                    spentAmount = doc.getDoubleSafe("spentAmount", 0.0),
                    iconName = doc.getStringSafe("iconName", "OTHER")
                )
            } ?: emptyList()

            // Get notifications
            val notifSnap = try {
                db.collection("users").document(uid).collection("notifications").get().await()
            } catch (e: Exception) { null }
            val notifications = notifSnap?.documents?.mapNotNull { doc ->
                val title = doc.getStringSafe("title", "").ifBlank { return@mapNotNull null }
                val rawId = doc.getLongSafe("id", -1L)
                val finalId = if (rawId > 0) rawId else (doc.id.hashCode().toLong().let { if (it < 0) -it else it })
                BankNotificationEntity(
                    id = finalId,
                    uid = uid,
                    title = title,
                    message = doc.getStringSafe("message", ""),
                    category = doc.getStringSafe("category", "Seguridad"),
                    timestamp = doc.getLongSafe("timestamp", System.currentTimeMillis()),
                    isRead = doc.getBooleanSafe("isRead", false),
                    amountTag = doc.getStringSafe("amountTag", "").ifBlank { null },
                    type = doc.getStringSafe("type", "GENERAL")
                )
            } ?: emptyList()

            // Get CIP and Security PIN
            val pinFromUser = userDoc.getStringSafe("securityPin", "").ifBlank {
                val pLong = userDoc.getLongSafe("securityPin", -1L)
                if (pLong >= 0) pLong.toString().padStart(6, '0') else ""
            }
            val userPin = if (pinFromUser.isNotBlank()) {
                pinFromUser.trim()
            } else {
                try {
                    val pinDoc = db.collection("security_pins").document(uid).get().await()
                    val p = pinDoc.getStringSafe("pin", "").ifBlank {
                        val pL = pinDoc.getLongSafe("pin", -1L)
                        if (pL >= 0) pL.toString().padStart(6, '0') else ""
                    }
                    p.trim()
                } catch (e: Exception) { "" }
            }

            var userCip = userDoc.getStringSafe("cipCode", "")
            if (userCip.isBlank() && profileComplete) {
                userCip = syncUserCipCodeToFirestore(
                    uid = uid,
                    fullName = fullName,
                    dni = dni,
                    phone = phone,
                    email = email,
                    accountNumber = account.accountNumber,
                    cciNumber = account.cciNumber
                ).getOrNull() ?: generateCleanCipCode(uid)
            }

            UserCloudData(
                fullName = fullName,
                dni = dni,
                phone = phone,
                email = email,
                accountType = accountType,
                role = "user",
                isBusiness = isBusiness,
                businessName = businessName,
                businessRuc = businessRuc,
                profileComplete = profileComplete,
                cipCode = userCip,
                securityPin = userPin,
                account = account,
                transactions = transactions,
                savingsGoals = goals,
                budgets = budgets,
                notifications = notifications
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data from Firestore: ${e.message}", e)
            null
        }
    }

    fun getCurrentUserEmail(): String? {
        if (!isFirebaseAvailable) return null
        return try {
            FirebaseAuth.getInstance().currentUser?.email
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendVerificationEmail(): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                user.sendEmailVerification().await()
                Log.d(TAG, "Email verification resent to ${user.email}")
                Result.success(Unit)
            } else {
                Result.failure(Exception("No se encontró usuario activo para reenviar el correo."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending verification email: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun checkIsEmailVerified(): Boolean {
        if (!isFirebaseAvailable) return true
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return false
            user.reload().await()
            val verified = user.isEmailVerified
            Log.d(TAG, "Email verification checked for ${user.email}: $verified")
            if (verified) {
                recordSecurityAuditLog(
                    uid = user.uid,
                    eventType = "EMAIL_VERIFIED",
                    details = "Correo electrónico verificado satisfactoriamente",
                    status = "SUCCESS",
                    userEmail = user.email ?: ""
                )
            }
            verified
        } catch (e: Exception) {
            Log.w(TAG, "Error checking email verification: ${e.message}")
            try {
                FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
            } catch (ex: Exception) {
                false
            }
        }
    }

    suspend fun deleteCurrentUserAccount(uid: String): Result<Unit> {
        if (!isFirebaseAvailable) return Result.success(Unit)
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email ?: ""
            recordSecurityAuditLog(
                uid = uid,
                eventType = "ACCOUNT_DELETED",
                details = "Cuenta de usuario y registros eliminados permanentemente",
                status = "SUCCESS",
                userEmail = email
            )

            try {
                val db = FirebaseFirestore.getInstance()
                val userDocRef = db.collection("users").document(uid)
                userDocRef.collection("account").document("main").delete().await()
                userDocRef.delete().await()
                Log.d(TAG, "Firestore record deleted for user: $uid")
            } catch (fsErr: Exception) {
                Log.w(TAG, "Firestore cleanup notice: ${fsErr.message}")
            }

            user?.delete()?.await()
            Log.d(TAG, "Firebase Auth account permanently deleted for $uid")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user account: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut(uid: String? = null, email: String? = null) {
        if (isFirebaseAvailable) {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Sign out error: ${e.message}")
            }
        }
    }

    fun getDeviceId(context: Context): String {
        return try {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "DEVICE_UNKNOWN"
        } catch (e: Exception) {
            "DEVICE_UNKNOWN"
        }
    }

    /**
     * Listens to global app system configuration (maintenance mode and forced updates)
     */
    fun listenToAppSystemConfig(onConfigChanged: (AppSystemConfig) -> Unit): ListenerRegistration? {
        if (!isFirebaseAvailable) {
            onConfigChanged(AppSystemConfig())
            return null
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("app_config").document("global")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "App config listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val config = AppSystemConfig(
                            maintenanceActive = snapshot.getBooleanSafe("maintenanceActive", false),
                            maintenanceType = snapshot.getStringSafe("maintenanceType", "TEMPORARY"),
                            maintenanceTitle = snapshot.getStringSafe("maintenanceTitle", "Mantenimiento Programado"),
                            maintenanceMessage = snapshot.getStringSafe("maintenanceMessage", "Estamos actualizando nuestros servidores bancarios."),
                            estimatedEnd = snapshot.getStringSafe("estimatedEnd", ""),
                            minVersionCode = snapshot.getLongSafe("minVersionCode", 1L).toInt(),
                            latestVersionCode = snapshot.getLongSafe("latestVersionCode", 1L).toInt(),
                            forceUpdate = snapshot.getBooleanSafe("forceUpdate", false),
                            updateUrl = snapshot.getStringSafe("updateUrl", "https://bcbank.pe/app-update"),
                            updateMessage = snapshot.getStringSafe("updateMessage", "Actualización de seguridad requerida para continuar.")
                        )
                        onConfigChanged(config)
                    } else {
                        onConfigChanged(AppSystemConfig())
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "listenToAppSystemConfig failed: ${e.message}")
            onConfigChanged(AppSystemConfig())
            null
        }
    }

    /**
     * Listens to whether THIS specific hardware device is banned/blocked for fraud
     */
    fun listenToDeviceBlock(deviceId: String, onBlockChanged: (BlockedDeviceInfo) -> Unit): ListenerRegistration? {
        if (!isFirebaseAvailable || deviceId.isBlank() || deviceId == "DEVICE_UNKNOWN") {
            onBlockChanged(BlockedDeviceInfo(deviceId = deviceId, isBlocked = false))
            return null
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("blocked_devices").document(deviceId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Device block listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val isBlocked = snapshot.getBooleanSafe("isBlocked", true)
                        val reason = snapshot.getStringSafe("reason", "Dispositivo bloqueado por prevención de fraudes y seguridad bancaria.")
                        val blockedAt = snapshot.getLongSafe("blockedAt", System.currentTimeMillis())
                        onBlockChanged(BlockedDeviceInfo(deviceId = deviceId, isBlocked = isBlocked, reason = reason, blockedAt = blockedAt))
                    } else {
                        onBlockChanged(BlockedDeviceInfo(deviceId = deviceId, isBlocked = false))
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "listenToDeviceBlock failed: ${e.message}")
            onBlockChanged(BlockedDeviceInfo(deviceId = deviceId, isBlocked = false))
            null
        }
    }

    /**
     * Listens to global broadcast announcements
     */
    fun listenToGlobalAnnouncements(onAnnouncements: (List<GlobalAnnouncement>) -> Unit): ListenerRegistration? {
        if (!isFirebaseAvailable) {
            onAnnouncements(emptyList())
            return null
        }
        return try {
            val db = FirebaseFirestore.getInstance()
            db.collection("global_announcements")
                .whereEqualTo("active", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Global announcements listen error: ${error.message}")
                        return@addSnapshotListener
                    }
                    val announcements = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            GlobalAnnouncement(
                                id = doc.id,
                                title = doc.getStringSafe("title", "Comunicado Oficial"),
                                message = doc.getStringSafe("message", ""),
                                category = doc.getStringSafe("category", "COMUNICADO_OFICIAL"),
                                priority = doc.getStringSafe("priority", "NORMAL"),
                                timestamp = doc.getLongSafe("timestamp", System.currentTimeMillis()),
                                actionUrl = doc.getStringSafe("actionUrl", ""),
                                active = doc.getBooleanSafe("active", true)
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }?.sortedByDescending { it.timestamp } ?: emptyList()
                    onAnnouncements(announcements)
                }
        } catch (e: Exception) {
            Log.w(TAG, "listenToGlobalAnnouncements error: ${e.message}")
            onAnnouncements(emptyList())
            null
        }
    }

    /**
     * Registers a verified official banking voucher with a unique 6-digit serial
     */
    suspend fun registerVoucherSerial(
        serial: String,
        opCode: String,
        amount: Double,
        title: String,
        senderOrRecipient: String,
        type: String,
        uid: String = ""
    ) {
        if (!isFirebaseAvailable || serial.isBlank()) return
        try {
            val db = FirebaseFirestore.getInstance()
            val now = System.currentTimeMillis()
            val voucherData = hashMapOf(
                "serial" to serial,
                "opCode" to opCode,
                "amount" to amount,
                "title" to title,
                "senderOrRecipient" to senderOrRecipient,
                "type" to type,
                "uid" to uid,
                "timestamp" to now,
                "status" to "VERIFIED",
                "issuer" to "BC-BANK PERÚ S.A.",
                "verificationHash" to "BC-HASH-${(serial + opCode + amount).hashCode()}"
            )
            db.collection("vouchers").document(serial).set(voucherData, SetOptions.merge()).await()
            Log.d(TAG, "Official voucher registered with serial: $serial")
        } catch (e: Exception) {
            Log.w(TAG, "registerVoucherSerial notice: ${e.message}")
        }
    }

    /**
     * Verifies an official BC-BANK voucher by its 6-digit serial number
     */
    suspend fun verifyVoucherBySerial(serial: String): VoucherVerificationResult {
        val cleanSerial = serial.trim().removePrefix("BCBANK-VOUCHER:").removePrefix("BC-").trim()
        if (cleanSerial.length !in 5..8 || !cleanSerial.all { it.isDigit() || it.isLetter() }) {
            return VoucherVerificationResult(
                isValid = false,
                serial = cleanSerial,
                isFraudulent = true,
                warningMessage = "El código ingresado o escaneado no tiene la estructura de un comprobante emitido por BC-BANK."
            )
        }

        if (!isFirebaseAvailable) {
            // Local fallback simulation for valid structure
            return VoucherVerificationResult(
                isValid = true,
                serial = cleanSerial,
                operationCode = "OP-$cleanSerial",
                transactionTitle = "Operación Bancaria Verificada",
                amount = 150.00,
                timestamp = System.currentTimeMillis(),
                senderOrRecipient = "Usuario Verificado BC-BANK",
                transactionType = "TRANSFER",
                isFraudulent = false
            )
        }

        return try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("vouchers").document(cleanSerial).get().await()
            if (doc.exists() && doc.getStringSafe("status", "") == "VERIFIED") {
                VoucherVerificationResult(
                    isValid = true,
                    serial = cleanSerial,
                    operationCode = doc.getStringSafe("opCode", "OP-$cleanSerial"),
                    transactionTitle = doc.getStringSafe("title", "Comprobante Oficial BC-BANK"),
                    amount = doc.getDoubleSafe("amount", 0.0),
                    timestamp = doc.getLongSafe("timestamp", System.currentTimeMillis()),
                    senderOrRecipient = doc.getStringSafe("senderOrRecipient", "Titular BC-BANK"),
                    transactionType = doc.getStringSafe("type", "TRANSFER"),
                    isFraudulent = false
                )
            } else {
                VoucherVerificationResult(
                    isValid = false,
                    serial = cleanSerial,
                    isFraudulent = true,
                    warningMessage = "Comprobante NO ENCONTRADO en el registro central de BC-BANK. Podría tratarse de un código adulterado o fraudulento."
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "verifyVoucherBySerial error: ${e.message}")
            VoucherVerificationResult(
                isValid = false,
                serial = cleanSerial,
                isFraudulent = true,
                warningMessage = "Error al consultar la base de datos de comprobantes: ${e.message}"
            )
        }
    }
}

data class AppSystemConfig(
    val maintenanceActive: Boolean = false,
    val maintenanceType: String = "TEMPORARY", // "TEMPORARY", "INDEFINITE"
    val maintenanceTitle: String = "Mantenimiento Programado de Sistemas",
    val maintenanceMessage: String = "Estamos realizando mejoras en nuestra infraestructura digital para optimizar la seguridad y velocidad de sus operaciones.",
    val estimatedEnd: String = "",
    val minVersionCode: Int = 1,
    val latestVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val updateUrl: String = "https://bcbank.pe/app-update",
    val updateMessage: String = "Hay una actualización crítica de seguridad requerida para continuar usando la banca móvil."
)

data class BlockedDeviceInfo(
    val deviceId: String,
    val isBlocked: Boolean = false,
    val reason: String = "Dispositivo restringido por infracción de seguridad o sospecha de fraude.",
    val blockedAt: Long = System.currentTimeMillis()
)

data class GlobalAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val category: String = "COMUNICADO_OFICIAL",
    val priority: String = "NORMAL", // NORMAL, HIGH, URGENT
    val timestamp: Long = System.currentTimeMillis(),
    val actionUrl: String = "",
    val active: Boolean = true
)

data class VoucherVerificationResult(
    val isValid: Boolean,
    val serial: String,
    val operationCode: String = "",
    val transactionTitle: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0L,
    val senderOrRecipient: String = "",
    val transactionType: String = "",
    val isFraudulent: Boolean = false,
    val warningMessage: String = ""
)

data class UserProfileData(
    val uid: String,
    val fullName: String,
    val dni: String,
    val phone: String,
    val email: String,
    val accountType: String,
    val role: String = "user",
    val isBusiness: Boolean = false,
    val businessName: String = "",
    val businessRuc: String = "",
    val birthDate: String = "",
    val isKid: Boolean = false,
    val age: Int = 18
)

data class CloudWithdrawal(
    val id: String,
    val uid: String,
    val userDni: String,
    val userPhone: String,
    val accountHolder: String,
    val amount: Double,
    val pinCode: String,
    val opCode: String,
    val qrData: String,
    val status: String, // PENDING, COMPLETED, CANCELLED, EXPIRED
    val createdAt: Long,
    val expiresAt: Long
)

data class PublicService(
    val id: String,
    val name: String,
    val category: String,
    val code: String,
    val fee: Double = 0.0,
    val commission: Double = 0.0,
    val description: String = "",
    val supplyCodeLabel: String = "Código de Suministro / N° de Recibo",
    val supplyCodePlaceholder: String = "Ej: 1849204",
    val supplyCodeMinLength: Int = 4,
    val active: Boolean = true,
    val priority: Int = 1
)

data class ServicePayment(
    val id: String,
    val uid: String,
    val accountHolder: String,
    val userDni: String,
    val userPhone: String = "",
    val serviceId: String,
    val serviceName: String,
    val serviceCategory: String,
    val supplyCode: String,
    val amount: Double,
    val commission: Double = 0.0,
    val totalPaid: Double = amount + commission,
    val operationCode: String = "",
    val comment: String = "",
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis(),
    val timestampFormatted: String = ""
)

data class UserSecurityPin(
    val uid: String,
    val pin: String,
    val status: String = "ACTIVE",
    val failedAttempts: Int = 0,
    val pinBlockedUntil: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val createdAtFormatted: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedAtFormatted: String = "",
    val lastVerifiedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

data class UserCipCode(
    val cipCode: String,
    val cleanCode: String,
    val uid: String,
    val accountHolder: String,
    val userDni: String,
    val userPhone: String = "",
    val userEmail: String = "",
    val accountNumber: String = "",
    val cciNumber: String = "",
    val bankName: String = "BC-BANK Perú",
    val status: String = "ACTIVE",
    val currency: String = "PEN",
    val totalDeposited: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val createdAtFormatted: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedAtFormatted: String = "",
    val qrData: String = ""
)

data class UserCloudData(
    val fullName: String,
    val dni: String,
    val phone: String = "",
    val email: String = "",
    val accountType: String = "",
    val role: String = "user",
    val isBusiness: Boolean = false,
    val businessName: String = "",
    val businessRuc: String = "",
    val profileComplete: Boolean = true,
    val cipCode: String = "",
    val securityPin: String = "",
    val account: AccountInfoEntity?,
    val transactions: List<TransactionEntity>,
    val savingsGoals: List<SavingsGoalEntity>,
    val budgets: List<BudgetEntity>,
    val notifications: List<BankNotificationEntity> = emptyList()
)

enum class AccountStatusType {
    ACTIVE,
    SUSPENDED,
    BLOCKED
}

data class AccountSecurityStatus(
    val status: AccountStatusType = AccountStatusType.ACTIVE,
    val title: String = "Cuenta Activa",
    val reason: String = "",
    val suspendedUntil: Long? = null,
    val blockedAt: Long? = null,
    val supportContact: String = "soporte@bcbank.pe",
    val supportPhone: String = "+51 915345098"
)

data class SupportChannel(
    val id: String,
    val type: String, // WHATSAPP, TELEGRAM, EMAIL, PHONE
    val title: String,
    val value: String,
    val description: String = "",
    val actionUrl: String = "",
    val isAvailable: Boolean = true,
    val isPrimary: Boolean = false,
    val priority: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

data class RecipientLookupResult(
    val fullName: String,
    val identifier: String,
    val bankName: String = "BC-BANK Perú",
    val found: Boolean
)


