package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"
    // Web client ID configured in Firebase project (blackcore-bank)
    const val WEB_CLIENT_ID = "5449433145-36p2t8me9kgivrmtpkjuphfvskmdaeov.apps.googleusercontent.com"

    fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val resolved = context.getString(resId)
                if (resolved.isNotBlank()) resolved else WEB_CLIENT_ID
            } else {
                WEB_CLIENT_ID
            }
        } catch (_: Exception) {
            WEB_CLIENT_ID
        }
    }

    suspend fun launchGoogleSignIn(
        context: Context,
        onSuccess: (idToken: String, email: String, displayName: String) -> Unit,
        onCancelled: (reason: String) -> Unit,
        onError: (errorMsg: String) -> Unit
    ) {
        val credentialManager = CredentialManager.create(context)
        try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val clientId = getWebClientId(context)
            Log.d(TAG, "Using Web Client ID for Google Sign In: $clientId")

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                onSuccess(idToken, email, displayName)
            } else {
                onError("Tipo de credencial no soportado: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign In cancelled by user")
            onCancelled("Inicio de sesión con Google cancelado por el usuario.")
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google Sign In error: ${e.message}")
            onError("No se pudo iniciar con Google: ${e.localizedMessage ?: e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected Google Auth error", e)
            onError("Error al conectar con Google: ${e.localizedMessage ?: e.message}")
        }
    }
}
