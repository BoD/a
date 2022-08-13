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
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.jraf.android.a.data.Data

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val launcherApps: LauncherApps = application.getSystemService(LauncherApps::class.java)
    private val allApps: List<App> = launcherApps.getActivityList(null, launcherApps.profiles[0])
        .map { launcherActivityInfo ->
            App(
                label = launcherActivityInfo.label.toString(),
                packageName = launcherActivityInfo.applicationInfo.packageName,
                activityName = launcherActivityInfo.name,
                drawable = launcherActivityInfo.getIcon(DisplayMetrics.DENSITY_XHIGH),
            )

        }
    private val data = Data(application)
    private var counters: Map<String, Int> = data.counters

    val searchQuery = MutableStateFlow("")
    val filteredApps: Flow<List<App>> = searchQuery.map { verbatimQuery ->
        val query = verbatimQuery.trim()
        allApps
            .filter { app ->
                app.label.contains(query, true) ||
                        app.packageName.contains(query, true)
            }
            .sortedByDescending {
                counters[it.packageName + "/" + it.activityName] ?: 0
            }
    }
    val intentToStart = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val scrollUp = MutableStateFlow(0)

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        scrollUp.value++
    }

    fun onAppClick(app: App) {
        val intent = Intent()
            .apply { setClassName(app.packageName, app.activityName) }
//            .setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intentToStart.tryEmit(intent)
        data.incrementCounter(app.packageName + "/" + app.activityName)
        counters = data.counters
    }

    fun resetSearchQuery() {
        onSearchQueryChange("")
    }

    class App(
        val label: String,
        val packageName: String,
        val activityName: String,
        val drawable: Drawable,
    )
}
