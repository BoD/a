/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2023-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.a.data

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.jraf.android.a.BuildConfig
import org.jraf.android.a.R
import org.jraf.android.a.util.Key
import org.jraf.android.a.util.invoke
import org.jraf.android.a.util.signalStateFlow

class AppRepository(context: Context) {
    companion object : Key<AppRepository>

    private val launcherApps: LauncherApps = context.getSystemService(LauncherApps::class.java)

    private val onPackagesChanged = signalStateFlow()

    init {
        launcherApps.registerCallback(
            object : LauncherApps.Callback() {
                override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
                    onPackagesChanged()
                }

                override fun onPackageAdded(packageName: String?, user: UserHandle?) {
                    onPackagesChanged()
                }

                override fun onPackageChanged(packageName: String?, user: UserHandle?) {
                    onPackagesChanged()
                }

                override fun onPackagesAvailable(
                    packageNames: Array<out String>?,
                    user: UserHandle?,
                    replacing: Boolean
                ) {
                    onPackagesChanged()
                }

                override fun onPackagesUnavailable(
                    packageNames: Array<out String>?,
                    user: UserHandle?,
                    replacing: Boolean
                ) {
                    onPackagesChanged()
                }
            })
    }

    data class App(
        val label: String,
        val packageName: String,
        val activityName: String,
        val drawable: Drawable,
    )

    private var firstLoad = true

    @OptIn(ExperimentalCoroutinesApi::class)
    val allApps: Flow<List<App>> = onPackagesChanged.flatMapLatest {
        flow {
            // On the first load, we first emit the apps without their icons to get something as fast as possible
            val launcherActivityInfos: List<LauncherActivityInfo> = launcherApps.getActivityList(null, launcherApps.profiles[0])
                .filter { launcherActivityInfo ->
                    // Don't show ourselves, unless we're in debug mode
                    BuildConfig.DEBUG || launcherActivityInfo.applicationInfo.packageName != context.packageName
                }
            if (firstLoad) {
                firstLoad = false
                val pendingDrawable = ContextCompat.getDrawable(context, R.drawable.pending)!!
                emit(
                    launcherActivityInfos.map { launcherActivityInfo ->
                        App(
                            label = launcherActivityInfo.label.toString(),
                            packageName = launcherActivityInfo.applicationInfo.packageName,
                            activityName = launcherActivityInfo.name,
                            drawable = pendingDrawable
                        )
                    }
                )
            }

            emit(
                launcherActivityInfos.map { launcherActivityInfo ->
                    App(
                        label = launcherActivityInfo.label.toString(),
                        packageName = launcherActivityInfo.applicationInfo.packageName,
                        activityName = launcherActivityInfo.name,
                        drawable = launcherActivityInfo.getIcon(DisplayMetrics.DENSITY_XHIGH)
                    )
                }
            )
        }
    }
        .flowOn(Dispatchers.IO)
}
