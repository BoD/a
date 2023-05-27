/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2022-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.a.ui.main

import android.app.Application
import android.app.SearchManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jraf.android.a.data.AppRepository
import org.jraf.android.a.data.ContactRepository
import org.jraf.android.a.data.LaunchItemRepository
import org.jraf.android.a.util.containsIgnoreAccents

private val DIFFERENT = object : Any() {
    override fun equals(other: Any?): Boolean {
        return false
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var allLaunchItems: MutableStateFlow<List<LaunchItem>> = MutableStateFlow(emptyList())

    private val launchItemRepository = LaunchItemRepository(application)
    private val appRepository = AppRepository(application, onPackagesChanged = ::refreshAllLaunchItems)
    private val contactRepository = ContactRepository(application)

    private val counters: MutableStateFlow<Map<String, Long>> = MutableStateFlow(emptyMap())

    init {
        viewModelScope.launch {
            launchItemRepository.counters.collect {
                counters.value = it
            }
        }

        refreshAllLaunchItems()
    }

    val searchQuery = MutableStateFlow("")
    val filteredLaunchItems: StateFlow<List<LaunchItem>> = allLaunchItems
        .combine(searchQuery) { allLaunchedItems, verbatimQuery ->
            val query = verbatimQuery.trim()
            allLaunchedItems
                .filter { it.matchesFilter(query) }
                .sortedByDescending {
                    counters.value[it.id] ?: 0
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val isKeyboardWebSearchActive: Flow<Boolean> = filteredLaunchItems.map { it.isEmpty() }

    val intentToStart = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val scrollUp: MutableStateFlow<Any> = MutableStateFlow(DIFFERENT)

    val shouldShowRequestPermissionRationale = MutableStateFlow(false)

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        scrollUp.value = DIFFERENT
    }

    fun onLaunchItemClick(launchedItem: LaunchItem) {
        intentToStart.tryEmit(launchedItem.clickIntent)
        viewModelScope.launch {
            launchItemRepository.recordLaunchedItem(launchedItem.id)
        }
    }

    fun onLaunchItemLongClick(launchedItem: LaunchItem) {
        launchedItem.longClickIntent?.let { intentToStart.tryEmit(it) }
    }

    fun resetSearchQuery() {
        onSearchQueryChange("")
    }

    private suspend fun getAllLaunchItems(): List<LaunchItem> {
        return appRepository.getAllApps().map { it.toAppLaunchItem() } +
                contactRepository.getStarredContacts().map { it.toContactLaunchItem() }
    }

    fun refreshAllLaunchItems() {
        viewModelScope.launch {
            allLaunchItems.value = getAllLaunchItems()
        }
    }

    fun onWebSearchClick() {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
            .putExtra(SearchManager.QUERY, searchQuery.value)
        intentToStart.tryEmit(intent)
    }

    fun onKeyboardActionButtonClick() {
        val launchItems = filteredLaunchItems.value
        if (searchQuery.value.isBlank()) return
        if (launchItems.isEmpty()) {
            onWebSearchClick()
        } else {
            onLaunchItemClick(launchItems.first())
        }
    }

    sealed interface LaunchItem {
        val label: String
        val drawable: Drawable
        val id: String
        val clickIntent: Intent
        val longClickIntent: Intent?

        fun matchesFilter(query: String): Boolean
    }

    data class AppLaunchItem(
        override val label: String,
        private val packageName: String,
        private val activityName: String,
        override val drawable: Drawable,
    ) : LaunchItem {
        override val id = "$packageName/$activityName"

        override val clickIntent = Intent()
            .apply { setClassName(packageName, activityName) }
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        override val longClickIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:$packageName"))

        override fun matchesFilter(query: String): Boolean {
            return label.containsIgnoreAccents(query) ||
                    packageName.contains(query, true)
        }
    }

    private fun AppRepository.App.toAppLaunchItem(): AppLaunchItem {
        return AppLaunchItem(
            label = label,
            packageName = packageName,
            activityName = activityName,
            drawable = drawable,
        )
    }

    data class ContactLaunchItem(
        override val label: String,
        private val contactId: Long,
        private val lookupKey: String,
        override val drawable: Drawable,
    ) : LaunchItem {
        override val id = lookupKey

        override val clickIntent = Intent(Intent.ACTION_VIEW)
            .setData(ContactsContract.Contacts.getLookupUri(contactId, lookupKey))

        override val longClickIntent = null

        override fun matchesFilter(query: String): Boolean {
            return label.containsIgnoreAccents(query)
        }
    }

    private fun ContactRepository.Contact.toContactLaunchItem(): ContactLaunchItem {
        return ContactLaunchItem(
            label = displayName,
            contactId = contactId,
            lookupKey = lookupKey,
            drawable = photoDrawable,
        )
    }
}
