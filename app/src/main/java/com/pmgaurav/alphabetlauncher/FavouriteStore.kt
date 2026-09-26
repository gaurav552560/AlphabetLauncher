package com.pmgaurav.alphabetlauncher

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.favouriteDataStore by preferencesDataStore(
    name = "favourite_apps"
)

class FavouriteStore(
    private val context: Context
) {

    companion object {
        private val FAVOURITE_PACKAGES =
            stringSetPreferencesKey("favourite_packages")
    }

    val favouritePackages: Flow<Set<String>> =
        context.favouriteDataStore.data.map { preferences ->
            preferences[FAVOURITE_PACKAGES] ?: emptySet()
        }

    suspend fun addFavourite(packageName: String) {
        context.favouriteDataStore.edit { preferences ->
            val current =
                preferences[FAVOURITE_PACKAGES] ?: emptySet()

            preferences[FAVOURITE_PACKAGES] =
                current + packageName
        }
    }

    suspend fun removeFavourite(packageName: String) {
        context.favouriteDataStore.edit { preferences ->
            val current =
                preferences[FAVOURITE_PACKAGES] ?: emptySet()

            preferences[FAVOURITE_PACKAGES] =
                current - packageName
        }
    }
}