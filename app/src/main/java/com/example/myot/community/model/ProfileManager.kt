package com.example.myot.community.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var profiles = mutableListOf<Profile>()
    private var selectedProfileId: Int? = null

    fun getProfiles(): List<Profile> = profiles

    fun addProfile(profile: Profile) {
        profiles.add(profile)
    }

    fun selectProfile(profileId: Int) {
        selectedProfileId = profileId
    }

    fun getSelectedProfile(): Profile? {
        return profiles.find { it.id == selectedProfileId }
    }

    fun deleteProfile(profileId: Int) {

    }
}
