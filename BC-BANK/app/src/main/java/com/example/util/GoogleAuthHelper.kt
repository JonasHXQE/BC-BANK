package com.example.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

object GoogleAuthHelper {

    suspend fun launchGoogleSignIn(
        context: Context,
        onSuccess: (idToken: String, email: String, displayName: String) -> Unit,
        onCancelled: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credentialManager = CredentialManager.create(context)

            val rawNonce = UUID.randomUUID().toString()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val webClientId = try {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resId != 0) context.getString(resId) else "5449433145-36p2t8me9kgivrmtpkjuphfvskmdaeov.apps.googleusercontent.com"
            } catch (e: Exception) {
                "5449433145-36p2t8me9kgivrmtpkjuphfvskmdaeov.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                onSuccess(
                    googleIdTokenCredential.idToken,
                    googleIdTokenCredential.id,
                    googleIdTokenCredential.displayName ?: ""
                )
            } else {
                onError("Tipo de credencial no reconocido")
            }
        } catch (e: GetCredentialCancellationException) {
            onCancelled("Inicio de sesión cancelado")
        } catch (e: GetCredentialException) {
            val msg = e.localizedMessage ?: e.message ?: ""
            if (msg.contains("cancel", ignoreCase = true) || msg.contains("16:", ignoreCase = true) || msg.contains("closed", ignoreCase = true)) {
                onCancelled("Inicio de sesión cancelado")
            } else {
                onError(msg.ifBlank { "Error al autenticar con Google" })
            }
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: e.message ?: ""
            if (msg.contains("cancel", ignoreCase = true) || msg.contains("16:", ignoreCase = true) || msg.contains("closed", ignoreCase = true)) {
                onCancelled("Inicio de sesión cancelado")
            } else {
                onError(msg.ifBlank { "Error inesperado al conectar con Google" })
            }
        }
    }
}
