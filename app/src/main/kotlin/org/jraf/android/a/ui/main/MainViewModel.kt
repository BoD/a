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
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings
import android.util.DisplayMetrics
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jraf.android.a.data.Data
import org.jraf.android.a.util.containsIgnoreAccents

private val DIFFERENT = object : Any() {
    override fun equals(other: Any?): Boolean {
        return false
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val launcherApps: LauncherApps = application.getSystemService(LauncherApps::class.java)
    private var allApps: MutableStateFlow<List<App>> = MutableStateFlow(emptyList())

    private val data = Data(application)

    private val counters: MutableStateFlow<Map<String, Int>> = MutableStateFlow(emptyMap())

    init {
        viewModelScope.launch {
            data.counters.collect {
                counters.value = it
            }
        }

        refreshAllApps()

        launcherApps.registerCallback(object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                refreshAllApps()
            }

            override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                refreshAllApps()
            }

            override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                refreshAllApps()
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean
            ) {
                refreshAllApps()
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>?,
                user: UserHandle?,
                replacing: Boolean
            ) {
                refreshAllApps()
            }
        })
    }

    val searchQuery = MutableStateFlow("")
    val filteredApps: StateFlow<List<App>> = allApps
        .combine(searchQuery) { allApps, verbatimQuery ->
            val query = verbatimQuery.trim()
            allApps
                .filter { app ->
                    app.label.containsIgnoreAccents(query) ||
                            app.packageName.contains(query, true)
                }
                .sortedByDescending {
                    counters.value[it.packageName + "/" + it.activityName] ?: 0
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val isKeyboardWebSearchActive: Flow<Boolean> = filteredApps.map { it.isEmpty() }

    val intentToStart = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val scrollUp: MutableStateFlow<Any> = MutableStateFlow(DIFFERENT)

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        scrollUp.value = DIFFERENT
    }

    fun onAppClick(app: App) {
        val intent = Intent()
            .apply { setClassName(app.packageName, app.activityName) }
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
//            .setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intentToStart.tryEmit(intent)
        viewModelScope.launch {
            data.incrementCounter(app.packageName + "/" + app.activityName)
        }
    }

    fun onAppLongClick(app: App) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:" + app.packageName))
        intentToStart.tryEmit(intent)
    }


    fun resetSearchQuery() {
        onSearchQueryChange("")
    }

    private suspend fun getAllApps(): List<App> = withContext(Dispatchers.IO) {
        launcherApps.getActivityList(null, launcherApps.profiles[0])
            .map { launcherActivityInfo ->
                App(
                    label = launcherActivityInfo.label.toString(),
                    packageName = launcherActivityInfo.applicationInfo.packageName,
                    activityName = launcherActivityInfo.name,
                    drawable = launcherActivityInfo.getIcon(DisplayMetrics.DENSITY_XHIGH),
                )
            }
    }

    private fun refreshAllApps() {
        viewModelScope.launch {
            allApps.value = getAllApps()
        }
    }

    fun onWebSearchClick() {
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
            .putExtra(SearchManager.QUERY, searchQuery.value)
        intentToStart.tryEmit(intent)
    }

    fun onKeyboardActionButtonClick() {
        val appList = filteredApps.value
        if (searchQuery.value.isBlank()) return
        if (appList.isEmpty()) {
            onWebSearchClick()
        } else {
            onAppClick(appList.first())
        }
    }

    class App(
        val label: String,
        val packageName: String,
        val activityName: String,
        val drawable: Drawable,
    )
}
