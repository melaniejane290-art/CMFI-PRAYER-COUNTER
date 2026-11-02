package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.model.UserAccount
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust local persistent account store for offline devotee accounts,
 * ensuring accounts, sign-ins, and session persistence function seamlessly.
 */
class LocalAccountStore(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("prayer_devotee_accounts", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_USER_JSON = "active_user_json"
        private const val KEY_REGISTERED_USERS = "registered_users_list"
        private const val LEGACY_SEED_ADMIN_UID = "admin_melanie_jane"
        private const val HASH_PREFIX = "pbkdf2"
        // HmacSHA256 is only available from API 26, and minSdk is 24.
        private const val HASH_ALGORITHM = "PBKDF2WithHmacSHA1"
        private const val HASH_ITERATIONS = 120_000
        private const val HASH_BITS = 256
    }

    init {
        migrateLegacyAccounts()
    }

    /**
     * Earlier versions seeded an admin account and stored passwords in clear text.
     */
    private fun migrateLegacyAccounts() {
        val stored = loadAllUsersWithPasswords()
        if (stored.none { it.first.uid == LEGACY_SEED_ADMIN_UID || isLegacyPassword(it.second) }) return
        saveAllUsers(
            stored.filter { it.first.uid != LEGACY_SEED_ADMIN_UID }
                .map { (user, pass) -> Pair(user, if (isLegacyPassword(pass)) hashPassword(pass) else pass) }
        )
        if (getActiveUser()?.uid == LEGACY_SEED_ADMIN_UID) setActiveUser(null)
    }

    private fun isLegacyPassword(stored: String): Boolean =
        stored.isNotEmpty() && !stored.startsWith("$HASH_PREFIX$")

    private fun hashPassword(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt, HASH_ITERATIONS)
        val encode = { bytes: ByteArray -> Base64.encodeToString(bytes, Base64.NO_WRAP) }
        return "$HASH_PREFIX$$HASH_ITERATIONS$${encode(salt)}$${encode(hash)}"
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, HASH_BITS)
        return SecretKeyFactory.getInstance(HASH_ALGORITHM).generateSecret(spec).encoded
    }

    /**
     * Checks a password against its stored hash. An account without one
     * (created through Google) can never be opened with a password.
     */
    fun passwordMatches(stored: String, password: String): Boolean {
        val parts = stored.split("$")
        if (parts.size != 4 || parts[0] != HASH_PREFIX) return false
        val salt = Base64.decode(parts[2], Base64.NO_WRAP)
        val expected = Base64.decode(parts[3], Base64.NO_WRAP)
        return MessageDigest.isEqual(derive(password, salt, parts[1].toInt()), expected)
    }

    fun saveUser(user: UserAccount, password: String) {
        val list = loadAllUsersWithPasswords().toMutableList()
        list.removeAll { it.first.email.equals(user.email, ignoreCase = true) }
        list.add(Pair(user, if (password.isEmpty()) "" else hashPassword(password)))
        saveAllUsers(list)
    }

    fun removeUser(email: String?) {
        val list = loadAllUsersWithPasswords().filterNot { it.first.email.equals(email, ignoreCase = true) }
        saveAllUsers(list)
    }

    fun findUserByEmail(email: String): Pair<UserAccount, String>? {
        val normalized = email.trim().lowercase()
        return loadAllUsersWithPasswords().firstOrNull { it.first.email?.lowercase() == normalized }
    }

    fun setActiveUser(user: UserAccount?) {
        if (user == null) {
            prefs.edit().remove(KEY_ACTIVE_USER_JSON).apply()
        } else {
            val json = userToJson(user).toString()
            prefs.edit().putString(KEY_ACTIVE_USER_JSON, json).apply()
        }
    }

    fun getActiveUser(): UserAccount? {
        val jsonStr = prefs.getString(KEY_ACTIVE_USER_JSON, null) ?: return null
        return try {
            jsonToUser(JSONObject(jsonStr))
        } catch (_: Exception) {
            null
        }
    }

    fun loadAllRegisteredUsers(): List<UserAccount> {
        return loadAllUsersWithPasswords().map { it.first }
    }

    private fun loadAllUsersWithPasswords(): List<Pair<UserAccount, String>> {
        val raw = prefs.getString(KEY_REGISTERED_USERS, null) ?: return emptyList()
        val result = mutableListOf<Pair<UserAccount, String>>()
        try {
            val jsonArray = JSONArray(raw)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val user = jsonToUser(obj.getJSONObject("user"))
                val pass = obj.optString("password", "")
                result.add(Pair(user, pass))
            }
        } catch (_: Exception) {}
        return result
    }

    private fun saveAllUsers(list: List<Pair<UserAccount, String>>) {
        val array = JSONArray()
        for (pair in list) {
            val obj = JSONObject()
            obj.put("user", userToJson(pair.first))
            obj.put("password", pair.second)
            array.put(obj)
        }
        prefs.edit().putString(KEY_REGISTERED_USERS, array.toString()).apply()
    }

    private fun userToJson(user: UserAccount): JSONObject {
        return JSONObject().apply {
            put("uid", user.uid)
            put("email", user.email ?: "")
            put("displayName", user.displayName ?: "")
            put("photoUrl", user.photoUrl ?: "")
            put("isAnonymous", user.isAnonymous)
            put("providerId", user.providerId)
            put("role", user.role)
            put("creationTimestamp", user.creationTimestamp)
            put("lastSignInTimestamp", user.lastSignInTimestamp)
        }
    }

    private fun jsonToUser(obj: JSONObject): UserAccount {
        return UserAccount(
            uid = obj.optString("uid"),
            email = obj.optString("email").takeIf { it.isNotBlank() },
            displayName = obj.optString("displayName").takeIf { it.isNotBlank() },
            photoUrl = obj.optString("photoUrl").takeIf { it.isNotBlank() },
            isAnonymous = obj.optBoolean("isAnonymous", false),
            providerId = obj.optString("providerId", "password"),
            // A locally stored account is never trusted as admin: only Firebase can grant it.
            role = "devotee",
            creationTimestamp = obj.optLong("creationTimestamp", System.currentTimeMillis()),
            lastSignInTimestamp = obj.optLong("lastSignInTimestamp", System.currentTimeMillis())
        )
    }
}
