package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PrayerSessionRecord
import com.example.model.AppLanguage
import com.example.model.HistoryTimeFilter
import com.example.viewmodel.PrayerCounterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Prayer Proclamation", appName)
  }

  @Test
  fun `prayer counter increments and saves cumulative progress`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = PrayerCounterViewModel(context)
    viewModel.updatePrayerTopic("Jesus Christ is the Lord")
    viewModel.updateTargetCount(1000)

    // Initial state
    assertEquals(0, viewModel.uiState.value.totalCount)
    assertEquals(0, viewModel.uiState.value.savedCumulativeCount)
    assertEquals(0, viewModel.uiState.value.currentSessionCount)

    // Increment 5 times in session
    repeat(5) { viewModel.incrementCount() }
    assertEquals(5, viewModel.uiState.value.totalCount)
    assertEquals(5, viewModel.uiState.value.currentSessionCount)
    assertEquals(0, viewModel.uiState.value.savedCumulativeCount)
    assertTrue(viewModel.uiState.value.isTimerRunning)

    // Save session progress
    viewModel.saveProgress()
    assertFalse(viewModel.uiState.value.isTimerRunning)
    assertEquals(5, viewModel.uiState.value.savedCumulativeCount)
    assertEquals(0, viewModel.uiState.value.currentSessionCount)
    assertEquals(5, viewModel.uiState.value.totalCount)

    // Second session: add 3 more
    repeat(3) { viewModel.incrementCount() }
    assertEquals(8, viewModel.uiState.value.totalCount)
    assertEquals(3, viewModel.uiState.value.currentSessionCount)
    assertEquals(5, viewModel.uiState.value.savedCumulativeCount)

    // Reset current session: saved cumulative count (5) MUST remain intact
    viewModel.resetSession()
    assertEquals(5, viewModel.uiState.value.totalCount)
    assertEquals(0, viewModel.uiState.value.currentSessionCount)
    assertEquals(5, viewModel.uiState.value.savedCumulativeCount)
    assertEquals(0L, viewModel.uiState.value.sessionDurationSeconds)
  }

  @Test
  fun `bilingual language switching to french updates state and presets`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = PrayerCounterViewModel(context)
    // Default is English
    assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
    assertEquals("Jesus Christ is the Lord", viewModel.uiState.value.prayerTopic)

    // Switch to French
    viewModel.setLanguage(AppLanguage.FRENCH)
    assertEquals(AppLanguage.FRENCH, viewModel.uiState.value.language)
    assertEquals("Jésus-Christ est Seigneur", viewModel.uiState.value.prayerTopic)

    // Increment and save in French
    repeat(10) { viewModel.incrementCount() }
    viewModel.saveProgress()

    assertTrue(viewModel.uiState.value.showSaveSuccessBanner)
    assertTrue(viewModel.uiState.value.saveMessage.contains("enregistrées"))
    assertEquals(10, viewModel.uiState.value.totalCount)

    // Switch back to English
    viewModel.setLanguage(AppLanguage.ENGLISH)
    assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.language)
    assertEquals("Jesus Christ is the Lord", viewModel.uiState.value.prayerTopic)
    assertEquals(10, viewModel.uiState.value.totalCount)
  }

  @Test
  fun `phase 3 session history formatting and sharing text`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = PrayerCounterViewModel(context)

    val session = PrayerSessionRecord(
        id = 1,
        topic = "I am the righteousness of God in Christ",
        sessionCount = 120,
        durationSeconds = 300,
        targetCount = 500,
        pacePerMinute = 24.0f,
        notes = "Felt deep peace during prayer."
    )

    // Test English share text
    val enShare = viewModel.formatSessionShareText(session, isFr = false)
    assertTrue(enShare.contains("Prayer Proclamation Session"))
    assertTrue(enShare.contains("+120"))
    assertTrue(enShare.contains("I am the righteousness of God in Christ"))
    assertTrue(enShare.contains("Felt deep peace"))

    // Test French share text
    val frShare = viewModel.formatSessionShareText(session, isFr = true)
    assertTrue(frShare.contains("Temps de Prière & Proclamation"))
    assertTrue(frShare.contains("+120"))
    assertTrue(frShare.contains("Proverbes 18:21"))

    // Test time filter switching
    viewModel.setHistoryFilter(HistoryTimeFilter.TODAY)
    assertEquals(HistoryTimeFilter.TODAY, viewModel.historyFilter.value)

    viewModel.setHistoryFilter(HistoryTimeFilter.ALL_TIME)
    assertEquals(HistoryTimeFilter.ALL_TIME, viewModel.historyFilter.value)
  }

  @Test
  fun `phase 4 firebase auth user model and viewmodel auth state`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = PrayerCounterViewModel(context)

    // Check initial auth state
    assertEquals(com.example.model.AuthMode.SIGN_IN, viewModel.authUiState.value.currentAuthMode)
    assertFalse(viewModel.authUiState.value.isLoading)
    assertFalse(viewModel.authUiState.value.isPasswordVisible)

    // Switch auth mode
    viewModel.setAuthMode(com.example.model.AuthMode.SIGN_UP)
    assertEquals(com.example.model.AuthMode.SIGN_UP, viewModel.authUiState.value.currentAuthMode)

    viewModel.togglePasswordVisibility()
    assertTrue(viewModel.authUiState.value.isPasswordVisible)

    viewModel.togglePasswordVisibility()
    assertFalse(viewModel.authUiState.value.isPasswordVisible)

    // UserAccount initials & display title test
    val user1 = com.example.model.UserAccount(
        uid = "usr123",
        email = "melaniejane290@gmail.com",
        displayName = "Melanie Jane"
    )
    assertEquals("Melanie Jane", user1.displayTitle)
    assertEquals("MJ", user1.initials)

    val user2 = com.example.model.UserAccount(
        uid = "usr456",
        email = "prayer.devotee@example.com",
        displayName = null
    )
    assertEquals("prayer.devotee", user2.displayTitle)
    assertEquals("PR", user2.initials)

    // Validation testing
    viewModel.signUpWithEmail("", "", "", "")
    assertTrue(viewModel.authUiState.value.errorMessage != null)

    viewModel.clearAuthMessages()
    assertEquals(null, viewModel.authUiState.value.errorMessage)
  }

  @Test
  fun `phase 6 user custom target setting and steppers adjustment`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = PrayerCounterViewModel(context)

    // Set custom target 777
    viewModel.updateTargetCount(777)
    assertEquals(777, viewModel.uiState.value.targetCount)

    // Adjust target by +500 -> 1277
    viewModel.adjustTargetBy(500)
    assertEquals(1277, viewModel.uiState.value.targetCount)

    // Adjust target by -100 -> 1177
    viewModel.adjustTargetBy(-100)
    assertEquals(1177, viewModel.uiState.value.targetCount)

    // Minimum boundary enforcement (cannot be < 1)
    viewModel.updateTargetCount(-50)
    assertEquals(1, viewModel.uiState.value.targetCount)

    // Target reached verification
    viewModel.updateTargetCount(10)
    repeat(10) { viewModel.incrementCount() }
    assertTrue(viewModel.uiState.value.isTargetReached)
    assertEquals(100, viewModel.uiState.value.progressPercentage)

    // Increase target -> no longer target reached
    viewModel.updateTargetCount(20)
    assertFalse(viewModel.uiState.value.isTargetReached)
    assertEquals(50, viewModel.uiState.value.progressPercentage)
  }

  @Test
  fun `firebase initialization and authentication validation`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val app = PrayerApplication()
    app.initializeFirebase()

    val authRepo = com.example.data.repository.AuthRepository(context)

    // Test sign up with AuthRepository directly
    val signUpResult = authRepo.signUpWithEmail("testdevotee@example.com", "password123", "Sister Grace")
    assertTrue(signUpResult.isSuccess)
    val user = signUpResult.getOrNull()
    assertNotNull(user)
    assertEquals("testdevotee@example.com", user?.email)
    assertEquals("Sister Grace", user?.displayName)

    // Test sign in with AuthRepository
    authRepo.signOut()
    assertEquals(null, authRepo.getCurrentUser())

    val signInResult = authRepo.signInWithEmail("testdevotee@example.com", "password123")
    assertTrue(signInResult.isSuccess)
    assertEquals("testdevotee@example.com", signInResult.getOrNull()?.email)

    // Test sign in with wrong password
    authRepo.signOut()
    val wrongPassResult = authRepo.signInWithEmail("testdevotee@example.com", "wrongpassword")
    assertTrue(wrongPassResult.isFailure)

    // Test Google Sign In fallback flow
    val googleResult = authRepo.signInWithGoogle(context)
    assertTrue(googleResult.isSuccess)
    val googleUser = googleResult.getOrNull()
    assertNotNull(googleUser)
    assertEquals("google.com", googleUser?.providerId)

    // Verify ViewModel auth validation
    val viewModel = PrayerCounterViewModel(context)
    viewModel.signUpWithEmail("devotee@example.com", "123", "123", "Prayer Warrior")
    assertTrue(viewModel.authUiState.value.errorMessage != null)

    viewModel.signUpWithEmail("devotee@example.com", "password123", "password456", "Prayer Warrior")
    assertTrue(viewModel.authUiState.value.errorMessage != null)

    viewModel.signInWithEmail("", "password123")
    assertTrue(viewModel.authUiState.value.errorMessage != null)

    viewModel.sendPasswordReset("")
    assertTrue(viewModel.authUiState.value.errorMessage != null)
  }

  @Test
  fun `admin role validation and access control without passcodes`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val authRepo = com.example.data.repository.AuthRepository(context)

    // Admin account sign in
    val adminResult = authRepo.signInWithEmail("melaniejane290@gmail.com", "securepass")
    assertTrue(adminResult.isSuccess)
    val admin = adminResult.getOrNull()
    assertNotNull(admin)
    assertTrue(admin?.isAdmin == true)
    assertEquals("admin", admin?.role)

    // Devotee account sign in
    val devoteeResult = authRepo.signUpWithEmail("devotee.member@example.com", "mypassword123", "Brother Paul")
    assertTrue(devoteeResult.isSuccess)
    val devotee = devoteeResult.getOrNull()
    assertNotNull(devotee)
    assertFalse(devotee?.isAdmin == true)
    assertEquals("devotee", devotee?.role)

    // ViewModel Admin Access Check
    val viewModel = PrayerCounterViewModel(context)
    viewModel.signInWithEmail("melaniejane290@gmail.com", "securepass")
    assertTrue(viewModel.checkAdminAccess())
    assertTrue(viewModel.isAdminUnlocked.value)
  }

  @Test
  fun `firestore repository cloud connectivity and sync methods`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val firestoreRepo = com.example.data.repository.FirestoreRepository()

    val testUser = com.example.model.UserAccount(
        uid = "test_user_777",
        email = "melaniejane290@gmail.com",
        displayName = "Melanie Jane",
        role = "admin"
    )

    // Test profile sync
    val profileResult = firestoreRepo.syncUserProfile(testUser, 1500, 3600L)
    assertNotNull(profileResult)

    // Test session record sync
    val sessionRecord = PrayerSessionRecord(
        id = 10,
        topic = "The Lord is my Shepherd",
        sessionCount = 500,
        durationSeconds = 1200,
        targetCount = 500,
        pacePerMinute = 25f
    )
    val sessionSyncResult = firestoreRepo.syncSessionToCloud(testUser.uid, sessionRecord)
    assertNotNull(sessionSyncResult)

    // Test Admin Broadcast Notification publish
    val notif = com.example.model.AdminBroadcastNotification(
        id = "test_broadcast_1",
        title = "Worldwide Prayer Vigil",
        message = "Join us this Friday for international intercession.",
        priority = com.example.model.NotificationPriority.URGENT_PRAYER,
        authorName = "Melanie Jane"
    )
    val notifResult = firestoreRepo.publishBroadcastNotification(notif)
    assertNotNull(notifResult)

    // Test Cloud Prayer Preset save and delete
    val preset = com.example.model.CloudPrayerPreset(
        id = "test_preset_1",
        textEn = "The Lord will fight for you",
        textFr = "L'Éternel combattra pour vous",
        category = "Deliverance"
    )
    val presetResult = firestoreRepo.saveCloudPreset(preset)
    assertNotNull(presetResult)

    // Test Devotee list fetch
    val devoteesResult = firestoreRepo.fetchAllDevotees()
    assertTrue(devoteesResult.isSuccess)
    val devotees = devoteesResult.getOrNull()
    assertNotNull(devotees)
    assertTrue(devotees!!.isNotEmpty())
  }
}
