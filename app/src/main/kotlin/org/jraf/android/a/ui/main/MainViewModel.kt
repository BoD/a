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
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jraf.android.a.data.AppRepository
import org.jraf.android.a.data.ContactRepository
import org.jraf.android.a.data.LaunchItemRepository
import org.jraf.android.a.data.NotificationRepository
import org.jraf.android.a.data.SettingsRepository
import org.jraf.android.a.data.ShortcutRepository
import org.jraf.android.a.get
import org.jraf.android.a.notification.NotificationListenerService
import org.jraf.android.a.util.containsIgnoreAccents
import org.jraf.android.a.util.invoke
import org.jraf.android.a.util.signalStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val launchItemRepository = LaunchItemRepository(application)
    private val appRepository = application[AppRepository]
    private val contactRepository = application[ContactRepository]
    private val shortcutRepository = application[ShortcutRepository]
    private val notificationRepository = application[NotificationRepository]
    private val settingsRepository = application[SettingsRepository]

    private val ignoredNotificationsItems = launchItemRepository.getIgnoredNotificationsItems()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val allLaunchItems: Flow<List<LaunchItem>> = appRepository.allApps.flatMapLatest { allApps ->
        val allShortcutsFlow = shortcutRepository.getAllShortcuts(allApps.map { it.packageName })
        combine(
            allShortcutsFlow,
            contactRepository.starredContacts,
            notificationRepository.notificationTimes,
            ignoredNotificationsItems,
            hasNotificationListenerPermission,
        ) { allShortcuts, starredContacts, notificationTimes, ignoredNotificationsItems, hasNotificationListenerPermission ->
            allApps.map { app ->
                val ignoreNotifications =
                    AppLaunchItem.getId(packageName = app.packageName, activityName = app.activityName) in ignoredNotificationsItems
                val notificationTime = if (!hasNotificationListenerPermission || ignoreNotifications) {
                    null
                } else {
                    notificationTimes[app.packageName]
                }
                app.toAppLaunchItem(
                    ignoreNotifications = ignoreNotifications,
                    notificationTime = notificationTime,
                )
            } +
                    allShortcuts.map { it.toShortcutLaunchItem() } +
                    starredContacts.map { it.toContactLaunchItem() }
        }
    }


    private val deletedLaunchItems = launchItemRepository.getDeletedItems()

    val searchQuery = MutableStateFlow("")
    val filteredLaunchItems: StateFlow<List<LaunchItem>> =
        combine(
            allLaunchItems,
            searchQuery,
            launchItemRepository.counters,
            deletedLaunchItems,
        ) { allLaunchedItems, verbatimQuery, counters, deletedLaunchItems ->
            val query = verbatimQuery.trim()
            allLaunchedItems
                .asSequence()
                .map {
                    if (it is AppLaunchItem && counters[it.id] == -1L) {
                        it.copy(isDeprioritized = true)
                    } else {
                        it
                    }
                }
                .filterNot { it.id in deletedLaunchItems }
                .filter { it.matchesFilter(query) }
                // First sort by counter (descending)
                .sortedByDescending {
                    counters[it.id] ?: 0
                }
                // then by notification time (descending)
                .sortedByDescending {
                    if (it.notificationTime != null && !it.ignoreNotifications) {
                        it.notificationTime!!
                    } else {
                        0
                    }
                }
                .toList()
        }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isKeyboardWebSearchActive: Flow<Boolean> = filteredLaunchItems.map { it.isEmpty() }

    val intentToStart = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val onScrollUp = signalStateFlow()

    val shouldShowRequestPermissionRationale = MutableStateFlow(false)

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        onScrollUp()
    }

    fun onLaunchItemPrimaryAction(launchedItem: LaunchItem) {
        when (launchedItem) {
            is AppLaunchItem -> intentToStart.tryEmit(launchedItem.launchAppIntent)
            is ContactLaunchItem -> intentToStart.tryEmit(launchedItem.viewContactIntent)
            is ShortcutLaunchItem -> shortcutRepository.launchShortcut(launchedItem.shortcut)
        }
        // Don't record the launch if there's a notification, as it's likely the user just wants to read it, that shouldn't count as a "real" launch
        if (!launchedItem.hasNotification) {
            viewModelScope.launch {
                // Add a delay so the reordering animation isn't distracting
                delay(1000)
                launchItemRepository.recordLaunchedItem(launchedItem.id)
            }
        }
    }

    fun onLaunchItemSecondaryAction(launchedItem: LaunchItem) {
        when (launchedItem) {
            is AppLaunchItem -> intentToStart.tryEmit(launchedItem.launchAppDetailsIntent)
            is ContactLaunchItem -> {
                launchedItem.sendSmsIntent?.let { intentToStart.tryEmit(it) }

                // Long clicking on a contact counts as a primary action
                viewModelScope.launch {
                    delay(1000)
                    launchItemRepository.recordLaunchedItem(launchedItem.id)
                }
            }

            is ShortcutLaunchItem -> viewModelScope.launch { launchItemRepository.deleteItem(launchedItem.id) }
        }
    }

    fun onLaunchItemTertiaryAction(launchedItem: LaunchItem) {
        viewModelScope.launch {
            if (launchedItem.isDeprioritized) {
                launchItemRepository.undeprioritizeItem(launchedItem.id)
            } else {
                launchItemRepository.deprioritizeItem(launchedItem.id)
            }
        }
    }

    fun onLaunchItemQuaternaryAction(launchedItem: LaunchItem) {
        viewModelScope.launch {
            if (launchedItem.ignoreNotifications) {
                launchItemRepository.unignoreNotifications(launchedItem.id)
            } else {
                launchItemRepository.ignoreNotifications(launchedItem.id)
            }
        }
    }

    fun resetSearchQuery() {
        onSearchQueryChange("")
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
            onLaunchItemPrimaryAction(launchItems.first())
        }
    }

    sealed class LaunchItem {
        abstract val label: String
        abstract val drawable: Drawable
        abstract val id: String
        abstract val isDeprioritized: Boolean
        abstract val ignoreNotifications: Boolean
        abstract val notificationTime: Long?

        val hasNotification: Boolean get() = notificationTime != null

        abstract fun matchesFilter(query: String): Boolean
    }

    data class AppLaunchItem(
        override val label: String,
        private val packageName: String,
        private val activityName: String,
        override val drawable: Drawable,
        override val isDeprioritized: Boolean,
        override val ignoreNotifications: Boolean,
        override val notificationTime: Long?,
    ) : LaunchItem() {
        override val id = getId(packageName, activityName)

        val launchAppIntent: Intent
            get() = Intent()
                .apply { setClassName(packageName, activityName) }
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        val launchAppDetailsIntent: Intent
            get() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:$packageName"))

        override fun matchesFilter(query: String): Boolean {
            return label.containsIgnoreAccents(query) ||
                    packageName.contains(query, true)
        }

        companion object {
            fun getId(packageName: String, activityName: String) = "${packageName}/${activityName}"
        }
    }

    private fun AppRepository.App.toAppLaunchItem(
        ignoreNotifications: Boolean,
        notificationTime: Long?,
    ): AppLaunchItem {
        return AppLaunchItem(
            label = label,
            packageName = packageName,
            activityName = activityName,
            drawable = drawable,
            isDeprioritized = false,
            ignoreNotifications = ignoreNotifications,
            notificationTime = notificationTime,
        )
    }

    data class ContactLaunchItem(
        override val label: String,
        private val contactId: Long,
        private val lookupKey: String,
        override val drawable: Drawable,
        private val phoneNumber: String?,
    ) : LaunchItem() {
        override val id = lookupKey

        val viewContactIntent: Intent
            get() = Intent(Intent.ACTION_VIEW)
                .setData(ContactsContract.Contacts.getLookupUri(contactId, lookupKey))

        val sendSmsIntent: Intent?
            get() = phoneNumber?.let {
                Intent(Intent.ACTION_SENDTO)
                    .setData(Uri.parse("smsto:$it"))
            }

        override fun matchesFilter(query: String): Boolean {
            return label.containsIgnoreAccents(query)
        }

        override val isDeprioritized: Boolean = false

        override val ignoreNotifications: Boolean = false

        override val notificationTime: Long? = null
    }

    private fun ContactRepository.Contact.toContactLaunchItem(): ContactLaunchItem {
        return ContactLaunchItem(
            label = displayName,
            contactId = contactId,
            lookupKey = lookupKey,
            drawable = photoDrawable,
            phoneNumber = phoneNumber,
        )
    }

    data class ShortcutLaunchItem(
        override val label: String,
        override val drawable: Drawable,
        val shortcut: ShortcutRepository.Shortcut,
    ) : LaunchItem() {
        override val id = "shortcut/${shortcut.id}"

        override fun matchesFilter(query: String): Boolean {
            return label.containsIgnoreAccents(query)
        }

        override val isDeprioritized: Boolean = false

        override val ignoreNotifications: Boolean = false

        override val notificationTime: Long? = null
    }

    private fun ShortcutRepository.Shortcut.toShortcutLaunchItem(): ShortcutLaunchItem {
        return ShortcutLaunchItem(
            label = label,
            drawable = drawable,
            shortcut = this,
        )
    }

    fun onContactsPermissionChanged() {
        contactRepository.onContactsPermissionChanged()
    }

    fun onRequestNotificationListenerPermissionClick() {
        settingsRepository.hasSeenRequestNotificationListenerPermissionBanner.value = true
        intentToStart.tryEmit(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                    .putExtra(
                        Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
                        ComponentName(getApplication(), NotificationListenerService::class.java).flattenToString()
                    )
            } else {
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            }
        )
    }

    val hasNotificationListenerPermissionSignal = signalStateFlow()
    val hasNotificationListenerPermission: Flow<Boolean> = hasNotificationListenerPermissionSignal.map {
        notificationRepository.hasNotificationListenerPermission()
    }.distinctUntilChanged()

    val hasSeenRequestNotificationListenerPermissionBanner: Flow<Boolean> =
        settingsRepository.hasSeenRequestNotificationListenerPermissionBanner
}
