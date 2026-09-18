package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserAccount
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
    }

    init {
        // Seed default admin account if not already present
        ensureSeedAdminAccount()
    }

    private fun ensureSeedAdminAccount() {
        val users = loadAllRegisteredUsers()
        if (users.none { it.email.equals("melaniejane290@gmail.com", ignoreCase = true) }) {
            val admin = UserAccount(
                uid = "admin_melanie_jane",
                email = "melaniejane290@gmail.com",
                displayName = "Melanie Jane",
                photoUrl = null,
                isAnonymous = false,
                providerId = "google.com",
                role = "admin",
                creationTimestamp = System.currentTimeMillis(),
                lastSignInTimestamp = System.currentTimeMillis()
            )
            saveUser(admin, "admin123")
        }
    }

    fun updateUserRole(uid: String, newRole: String) {
        val list = loadAllUsersWithPasswords().toMutableList()
        val index = list.indexOfFirst { it.first.uid == uid }
        if (index >= 0) {
            val (user, pass) = list[index]
            val updated = user.copy(role = newRole)
            list[index] = Pair(updated, pass)
            saveAllUsers(list)
            if (getActiveUser()?.uid == uid) {
                setActiveUser(updated)
            }
        }
    }

    fun saveUser(user: UserAccount, password: String) {
        val list = loadAllUsersWithPasswords().toMutableList()
        list.removeAll { it.first.email.equals(user.email, ignoreCase = true) }
        list.add(Pair(user, password))
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
            role = obj.optString("role", "devotee"),
            creationTimestamp = obj.optLong("creationTimestamp", System.currentTimeMillis()),
            lastSignInTimestamp = obj.optLong("lastSignInTimestamp", System.currentTimeMillis())
        )
    }
}
