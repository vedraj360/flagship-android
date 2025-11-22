package com.vdx.flagship.internal

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vdx.flagship.model.FeatureFlag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flagship_store")

internal class FlagStore(private val context: Context) {
    private val gson = Gson()
    private val FLAGS_KEY = stringPreferencesKey("cached_flags_json")

    suspend fun saveFlags(flags: List<FeatureFlag>) {
        val json = gson.toJson(flags)
        context.dataStore.edit { preferences ->
            preferences[FLAGS_KEY] = json
        }
    }

    val flagsFlow: Flow<Map<String, FeatureFlag>> = context.dataStore.data
        .map { preferences ->
            val json = preferences[FLAGS_KEY]
            if (json.isNullOrEmpty()) {
                emptyMap()
            } else {
                try {
                    val type = object : TypeToken<List<FeatureFlag>>() {}.type
                    val list: List<FeatureFlag> = gson.fromJson(json, type)
                    list.associateBy { it.key }
                } catch (e: Exception) {
                    emptyMap()
                }
            }
        }
}
