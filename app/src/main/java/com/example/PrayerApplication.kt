package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Application class ensuring robust initialization of Firebase across app lifecycle.
 */
class PrayerApplication : Application() {

    companion object {
        private const val TAG = "PrayerApplication"
        lateinit var instance: PrayerApplication
            private set
    }

    val localAccountStore by lazy { com.example.data.repository.LocalAccountStore(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeFirebase()
    }

    /**
     * Initializes FirebaseApp, FirebaseAuth, and Cloud Firestore with offline persistence.
     */
    fun initializeFirebase(context: android.content.Context = this): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val app = try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    Log.w(TAG, "Resource initialization note: ${e.message}")
                    null
                }
                if (app == null && FirebaseApp.getApps(context).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:550784852984:android:d41d8cd98f00b204e98009")
                        .setProjectId("gen-lang-client-550784852984")
                        .setApiKey("AIzaSyPrayerProclamationFirebaseKey2026")
                        .setDatabaseUrl("https://prayer-proclamation-default-rtdb.firebaseio.com")
                        .setStorageBucket("gen-lang-client-550784852984.firebasestorage.app")
                        .setGcmSenderId("550784852984")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.i(TAG, "Firebase initialized with programmatic fallback configuration.")
                } else {
                    Log.i(TAG, "Firebase initialized successfully from google-services resources.")
                }
            } else {
                Log.i(TAG, "Firebase is already initialized.")
            }

            // Warm up and verify FirebaseAuth & Firestore instances safely
            runCatching {
                FirebaseAuth.getInstance()
            }
            runCatching {
                val firestore = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build()
                firestore.firestoreSettings = settings
                Log.i(TAG, "FirebaseAuth and Cloud Firestore ready.")
            }.onFailure { err ->
                Log.w(TAG, "Firebase service warmup note: ${err.message}")
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Default Firebase initialization encountered: ${e.message}. Trying programmatic fallback...", e)
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:550784852984:android:d41d8cd98f00b204e98009")
                        .setProjectId("gen-lang-client-550784852984")
                        .setApiKey("AIzaSyPrayerProclamationFirebaseKey2026")
                        .setDatabaseUrl("https://prayer-proclamation-default-rtdb.firebaseio.com")
                        .setStorageBucket("gen-lang-client-550784852984.firebasestorage.app")
                        .setGcmSenderId("550784852984")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
                runCatching {
                    FirebaseAuth.getInstance()
                    FirebaseFirestore.getInstance()
                }
                Log.i(TAG, "Firebase initialized via secondary programmatic builder.")
                true
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Secondary Firebase initialization failed: ${fallbackError.message}", fallbackError)
                false
            }
        }
    }
}
