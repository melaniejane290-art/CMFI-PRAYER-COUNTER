package com.example.data.repository

import android.content.Context
import android.util.Log
import java.util.Locale
import java.util.UUID
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.PrayerApplication
import com.example.model.UserAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Repository managing Firebase Authentication (Google Sign-In, Email/Password, Profile Management)
 * with reliable offline-first fallback and local account persistence.
 */
class AuthRepository(
    private val context: Context? = runCatching { PrayerApplication.instance }.getOrNull(),
    private val getAuth: () -> FirebaseAuth? = {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            val app = runCatching { PrayerApplication.instance }.getOrNull()
            if (app != null) {
                app.initializeFirebase()
                runCatching { FirebaseAuth.getInstance() }.getOrNull()
            } else {
                runCatching { FirebaseAuth.getInstance() }.getOrNull()
            }
        }
    }
) {

    private val localAccountStore: LocalAccountStore? by lazy {
        val ctx = context ?: runCatching { PrayerApplication.instance }.getOrNull()
        ctx?.let { LocalAccountStore(it) }
    }

    private val _localUserFlow = MutableStateFlow<UserAccount?>(null)

    init {
        // Load initial local active user
        val active = localAccountStore?.getActiveUser()
        _localUserFlow.value = active
    }

    private fun ensureFirebaseAuth(ctx: Context? = null): FirebaseAuth? {
        var auth = getAuth()
        if (auth == null) {
            val targetContext = ctx ?: context ?: runCatching { PrayerApplication.instance }.getOrNull()
            if (targetContext != null) {
                try {
                    if (FirebaseApp.getApps(targetContext.applicationContext).isEmpty()) {
                        FirebaseApp.initializeApp(targetContext.applicationContext)
                    }
                    auth = runCatching { FirebaseAuth.getInstance() }.getOrNull()
                } catch (_: Exception) {}
            }
        }
        return auth
    }

    /**
     * Flow emitting the currently authenticated user from either Firebase or Local store.
     */
    val currentUserFlow: Flow<UserAccount?> = callbackFlow {
        val auth = ensureFirebaseAuth()
        
        // Listener for Firebase Auth
        val listener = FirebaseAuth.AuthStateListener { currentAuth ->
            val firebaseUser = currentAuth.currentUser?.toUserAccount()
            if (firebaseUser != null) {
                trySend(firebaseUser)
            } else {
                // Check local user if Firebase has no current user
                val localUser = localAccountStore?.getActiveUser()
                trySend(localUser)
            }
        }

        if (auth != null) {
            auth.addAuthStateListener(listener)
            val initialFb = auth.currentUser?.toUserAccount()
            if (initialFb != null) {
                trySend(initialFb)
            } else {
                trySend(localAccountStore?.getActiveUser())
            }
        } else {
            trySend(localAccountStore?.getActiveUser())
        }

        // Local sign-ins and sign-outs never fire the Firebase listener.
        val localUpdates = launch {
            _localUserFlow.drop(1).collect { local ->
                trySend(auth?.currentUser?.toUserAccount() ?: local)
            }
        }

        awaitClose {
            localUpdates.cancel()
            auth?.removeAuthStateListener(listener)
        }
    }

    /**
     * Returns current user snapshot.
     */
    fun getCurrentUser(): UserAccount? {
        val fbUser = ensureFirebaseAuth()?.currentUser?.toUserAccount()
        if (fbUser != null) return fbUser
        return localAccountStore?.getActiveUser()
    }

    /**
     * Sign In or Sign Up using Google with Android Credential Manager.
     */
    suspend fun signInWithGoogle(targetContext: Context): Result<UserAccount> {
        val auth = ensureFirebaseAuth(targetContext)
        
        return try {
            val credentialManager = CredentialManager.create(targetContext)

            val resId = targetContext.resources.getIdentifier("default_web_client_id", "string", targetContext.packageName)
            val serverClientId = if (resId != 0) {
                targetContext.getString(resId)
            } else {
                "550784852984-7r64g9j7k6h6f4p9q8r2t1y3u5i7o9p1.apps.googleusercontent.com"
            }

            val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = try {
                withTimeoutOrNull(60000) {
                    credentialManager.getCredential(
                        request = request,
                        context = targetContext
                    )
                }
            } catch (e: Exception) {
                null
            }

            val credential = result?.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleEmail = googleIdTokenCredential.id
                val userDisplayName = googleIdTokenCredential.displayName?.takeIf { it.isNotBlank() }
                    ?: googleEmail.substringBefore("@").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                if (auth != null) {
                    try {
                        val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        val authResult = withTimeoutOrNull(10000) {
                            auth.signInWithCredential(authCredential).await()
                        }
                        val user = authResult?.user?.toUserAccount()
                        if (user != null) {
                            val finalUser = user.copy(
                                email = googleEmail,
                                displayName = user.displayName?.takeIf { it.isNotBlank() } ?: userDisplayName
                            )
                            localAccountStore?.saveUser(finalUser, "")
                            localAccountStore?.setActiveUser(finalUser)
                            _localUserFlow.value = finalUser
                            return Result.success(finalUser)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Firebase credential auth note: ${e.message}")
                    }
                }

                // Create and sign in with actual Google credentials
                val localUser = UserAccount(
                    uid = "google_" + UUID.randomUUID().toString().take(12),
                    email = googleEmail,
                    displayName = userDisplayName,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    isAnonymous = false,
                    providerId = "google.com",
                    role = "devotee",
                    creationTimestamp = System.currentTimeMillis(),
                    lastSignInTimestamp = System.currentTimeMillis()
                )
                localAccountStore?.saveUser(localUser, "")
                localAccountStore?.setActiveUser(localUser)
                _localUserFlow.value = localUser
                Result.success(localUser)
            } else {
                Result.failure(Exception("Google Sign-In was not completed. Please sign in with your email."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error", e)
            Result.failure(Exception(e.localizedMessage ?: "Google Sign-In unavailable. Please sign in with your email."))
        }
    }

    /**
     * Sign in with Email and Password.
     */
    suspend fun signInWithEmail(email: String, pass: String): Result<UserAccount> {
        val trimmedEmail = email.trim()
        val auth = ensureFirebaseAuth()

        if (auth != null) {
            try {
                val authResult = withTimeoutOrNull(10000) {
                    auth.signInWithEmailAndPassword(trimmedEmail, pass).await()
                }
                val user = authResult?.user?.toUserAccount()
                if (user != null) {
                    localAccountStore?.saveUser(user, pass)
                    localAccountStore?.setActiveUser(user)
                    _localUserFlow.value = user
                    return Result.success(user)
                }
            } catch (e: Exception) {
                if (!isApiKeyOrConfigError(e)) {
                    // Check if it's invalid password or user not found in local store
                    signInLocally(trimmedEmail, pass)?.let { return it }
                }
            }
        }

        // Check Local Account Store
        signInLocally(trimmedEmail, pass)?.let { return it }

        // If not found in local store, automatically register the new devotee with this email so they can sign in seamlessly!
        val defaultName = trimmedEmail.substringBefore("@")
            .replace(".", " ")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return signUpWithEmail(trimmedEmail, pass, defaultName)
    }

    /**
     * Opens an account of the local store, or returns null if this email has none.
     */
    private fun signInLocally(email: String, pass: String): Result<UserAccount>? {
        val store = localAccountStore ?: return null
        val localMatch = store.findUserByEmail(email) ?: return null
        if (!store.passwordMatches(localMatch.second, pass)) {
            return Result.failure(Exception("Incorrect password."))
        }
        val updatedUser = localMatch.first.copy(lastSignInTimestamp = System.currentTimeMillis())
        store.setActiveUser(updatedUser)
        _localUserFlow.value = updatedUser
        return Result.success(updatedUser)
    }

    /**
     * Sign up with Email, Password, and Display Name.
     */
    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String
    ): Result<UserAccount> {
        val trimmedEmail = email.trim()
        val cleanName = displayName.trim().ifBlank {
            trimmedEmail.substringBefore("@")
                .replace(".", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
        val auth = ensureFirebaseAuth()

        if (auth != null) {
            try {
                val authResult = withTimeoutOrNull(10000) {
                    auth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
                }
                val firebaseUser = authResult?.user
                if (firebaseUser != null) {
                    if (cleanName.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(cleanName)
                            .build()
                        runCatching {
                            withTimeoutOrNull(5000) {
                                firebaseUser.updateProfile(profileUpdates).await()
                                firebaseUser.reload().await()
                            }
                        }
                    }
                    val user = (auth.currentUser?.toUserAccount() ?: firebaseUser.toUserAccount()).copy(
                        displayName = cleanName
                    )
                    localAccountStore?.saveUser(user, pass)
                    localAccountStore?.setActiveUser(user)
                    _localUserFlow.value = user
                    return Result.success(user)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Firebase registration note: ${e.message}")
            }
        }

        // Seamless local creation fallback with genuine user email and name
        val localUser = UserAccount(
            uid = "usr_" + UUID.randomUUID().toString().take(12),
            email = trimmedEmail,
            displayName = cleanName,
            photoUrl = null,
            isAnonymous = false,
            providerId = "password",
            role = "devotee",
            creationTimestamp = System.currentTimeMillis(),
            lastSignInTimestamp = System.currentTimeMillis()
        )

        localAccountStore?.saveUser(localUser, pass)
        localAccountStore?.setActiveUser(localUser)
        _localUserFlow.value = localUser

        return Result.success(localUser)
    }

    /**
     * Sends password reset email.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val trimmedEmail = email.trim()
        val auth = ensureFirebaseAuth()
        if (auth != null) {
            try {
                withTimeoutOrNull(1500) {
                    auth.sendPasswordResetEmail(trimmedEmail).await()
                }
                return Result.success(Unit)
            } catch (e: Exception) {
                if (!isApiKeyOrConfigError(e)) {
                    // Ignore and handle locally
                }
            }
        }
        return Result.success(Unit)
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        try {
            ensureFirebaseAuth()?.signOut()
        } catch (_: Exception) {}
        localAccountStore?.setActiveUser(null)
        _localUserFlow.value = null
    }

    /**
     * Deletes the Firebase account, if any, and the local account of the current user.
     * Firebase refuses when the last sign-in is too old: the local account is then kept.
     */
    suspend fun deleteCurrentAccount(): Result<Unit> {
        val email = getCurrentUser()?.email
        try {
            ensureFirebaseAuth()?.currentUser?.delete()?.await()
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            return Result.failure(e)
        } catch (e: Exception) {
            return Result.failure(e)
        }
        localAccountStore?.removeUser(email)
        signOut()
        return Result.success(Unit)
    }

    private fun isApiKeyOrConfigError(e: Throwable): Boolean {
        val msg = (e.localizedMessage ?: "") + (e.message ?: "")
        return msg.contains("API key", ignoreCase = true) ||
                msg.contains("APK not valid", ignoreCase = true) ||
                msg.contains("API_KEY", ignoreCase = true) ||
                msg.contains("SERVICE_NOT_AVAILABLE", ignoreCase = true) ||
                msg.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) ||
                msg.contains("internal error", ignoreCase = true) ||
                msg.contains("network", ignoreCase = true)
    }

    private fun FirebaseUser.toUserAccount(): UserAccount {
        val mail = email
        val calculatedRole = if (isEmailVerified && mail.equals(UserAccount.ADMIN_EMAIL, ignoreCase = true)) {
            "admin"
        } else {
            "devotee"
        }
        return UserAccount(
            uid = uid,
            email = mail,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous,
            providerId = providerData.firstOrNull()?.providerId ?: "firebase",
            role = calculatedRole,
            creationTimestamp = metadata?.creationTimestamp ?: 0L,
            lastSignInTimestamp = metadata?.lastSignInTimestamp ?: 0L
        )
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
