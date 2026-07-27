package com.aiagents.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.aiagents.app.data.model.ApiKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val GEMINI = stringPreferencesKey("gemini_key")
        val DEEPSEEK = stringPreferencesKey("deepseek_key")
        val QWEN = stringPreferencesKey("qwen_key")
        val MISTRAL = stringPreferencesKey("mistral_key")
    }

    val apiKeys: Flow<ApiKeys> = context.dataStore.data.map { prefs ->
        ApiKeys(
            gemini = prefs[Keys.GEMINI] ?: "",
            deepseek = prefs[Keys.DEEPSEEK] ?: "",
            qwen = prefs[Keys.QWEN] ?: "",
            mistral = prefs[Keys.MISTRAL] ?: ""
        )
    }

    suspend fun saveKeys(keys: ApiKeys) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GEMINI] = keys.gemini
            prefs[Keys.DEEPSEEK] = keys.deepseek
            prefs[Keys.QWEN] = keys.qwen
            prefs[Keys.MISTRAL] = keys.mistral
        }
    }

    suspend fun clearKeys() {
        context.dataStore.edit { it.clear() }
    }
}
