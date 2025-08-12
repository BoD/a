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

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
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

    private val launcherApps: LauncherApps = context.getSystemService()!!
    private val userManager: UserManager = context.getSystemService<UserManager>()!!


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
                    replacing: Boolean,
                ) {
                    onPackagesChanged()
                }

                override fun onPackagesUnavailable(
                    packageNames: Array<out String>?,
                    user: UserHandle?,
                    replacing: Boolean,
                ) {
                    onPackagesChanged()
                }
            },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    onPackagesChanged()
                }
            }
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                IntentFilter(Intent.ACTION_PROFILE_AVAILABLE),
                ContextCompat.RECEIVER_EXPORTED,
            )
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                IntentFilter(Intent.ACTION_PROFILE_UNAVAILABLE),
                ContextCompat.RECEIVER_EXPORTED,
            )
        }
    }

    data class App(
        val label: String,
        val drawable: Drawable,
        val componentName: ComponentName,
        val user: UserHandle,
        val isPrivateSpaceLocked: Boolean,
    )

    private var firstLoad = true

    @OptIn(ExperimentalCoroutinesApi::class)
    val allApps: Flow<List<App>> = onPackagesChanged.flatMapLatest {
        flow {
            // On the first load, we first emit the apps without their icons to get something as fast as possible
            val launcherActivityInfos: List<LauncherActivityInfo> = launcherApps.profiles.flatMap { profile ->
                launcherApps.getActivityList(null, profile)
                    .filter { launcherActivityInfo ->
                        // Don't show ourselves, unless we're in debug mode
                        BuildConfig.DEBUG || launcherActivityInfo.applicationInfo.packageName != context.packageName
                    }
            }
            if (firstLoad) {
                firstLoad = false
                val pendingDrawable = ContextCompat.getDrawable(context, R.drawable.pending)!!
                emit(
                    launcherActivityInfos.map { launcherActivityInfo ->
                        App(
                            label = launcherActivityInfo.label.toString(),
                            drawable = pendingDrawable,
                            componentName = launcherActivityInfo.getComponentName(),
                            user = launcherActivityInfo.user,
                            isPrivateSpaceLocked = userManager.isQuietModeEnabled(launcherActivityInfo.user),
                        )
                    },
                )
            }

            emit(
                launcherActivityInfos.map { launcherActivityInfo ->
                    App(
                        label = launcherActivityInfo.label.toString(),
                        drawable = launcherActivityInfo.getIcon(DisplayMetrics.DENSITY_XHIGH),
                        componentName = launcherActivityInfo.getComponentName(),
                        user = launcherActivityInfo.user,
                        isPrivateSpaceLocked = userManager.isQuietModeEnabled(launcherActivityInfo.user),
                    )
                },
            )
        }
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
}
