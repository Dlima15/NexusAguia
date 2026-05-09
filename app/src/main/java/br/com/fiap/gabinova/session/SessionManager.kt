package br.com.fiap.gabinova.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gab_session")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN     = stringPreferencesKey("token")
        private val KEY_USER_ID   = stringPreferencesKey("user_id")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
        private val KEY_EMAIL     = stringPreferencesKey("email")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_TOKEN]?.isNotBlank() == true }

    val tokenFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_TOKEN] ?: "" }

    val userIdFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_ID] ?: "" }

    val userNameFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_NAME] ?: "" }

    val userRoleFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_ROLE] ?: "" }

    val emailFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_EMAIL] ?: "" }

    suspend fun saveSession(
        token: String,
        userId: String,
        userName: String,
        userRole: String,
        email: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN]     = token
            prefs[KEY_USER_ID]   = userId
            prefs[KEY_USER_NAME] = userName
            prefs[KEY_USER_ROLE] = userRole
            prefs[KEY_EMAIL]     = email
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
