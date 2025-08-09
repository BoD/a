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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jraf.android.a.ui.main.MainViewModel.LaunchItem
import org.jraf.android.a.util.invoke
import org.jraf.android.a.util.logd

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        logd("Permission granted: $granted")
        viewModel.onContactsPermissionChanged()
        viewModel.shouldShowRequestPermissionRationale.value =
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                viewModel.shouldShowRequestPermissionRationale.value = true
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        lifecycleScope.launch {
            viewModel.destination.collect { (intent, user) ->
                if (user == null) {
                    startActivity(intent)
                } else {
                    getSystemService<LauncherApps>()!!.startMainActivity(intent.component, user, intent.sourceBounds, null);
                }
            }
        }

        setContent {
            val searchQuery: String by viewModel.searchQuery.collectAsState()
            val hasNotifications: Boolean by viewModel.hasNotifications.collectAsState()
            val launchItems: List<LaunchItem> by viewModel.filteredLaunchItems.collectAsState()
            val isKeyboardWebSearchActive: Boolean by viewModel.isKeyboardWebSearchActive.collectAsState(
                initial = false,
            )
            val scrollUp: Any by viewModel.onScrollUp.collectAsState()
            val shouldShowRequestPermissionRationale: Boolean by viewModel.shouldShowRequestPermissionRationale.collectAsState()
            val hasNotificationListenerPermission: Boolean by viewModel.hasNotificationListenerPermission.collectAsState(
                initial = true,
            )
            val hasSeenRequestNotificationListenerPermissionBanner: Boolean by viewModel.hasSeenRequestNotificationListenerPermissionBanner.collectAsState()
            val alignmentBottom: Boolean by viewModel.alignmentBottom.collectAsState()
            val alignmentRight: Boolean by viewModel.alignmentRight.collectAsState()
            val wallpaperOpacity: Float by viewModel.wallpaperOpacity.collectAsState()
            val showNotificationsButton: Boolean by viewModel.showNotificationsButton.collectAsState()
            val keyboardHack: Boolean by viewModel.keyboardHack.collectAsState()

            val gridState = rememberLazyGridState()

            LaunchedEffect(scrollUp) {
                delay(225)
                gridState.animateScrollToItem(0)
            }

            MainLayout(
                searchQuery = searchQuery,
                hasNotifications = hasNotifications,
                launchItems = launchItems,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onResetSearchQueryClick = viewModel::resetSearchQuery,
                onWebSearchClick = viewModel::onWebSearchClick,
                onKeyboardActionButtonClick = viewModel::onKeyboardActionButtonClick,
                isKeyboardWebSearchActive = isKeyboardWebSearchActive,
                onLaunchItemAction1 = viewModel::onLaunchItemAction1,
                onLaunchItemAction2 = viewModel::onLaunchItemAction2,
                onLaunchItemAction3 = viewModel::onLaunchItemAction3,
                onLaunchItemAction4 = viewModel::onLaunchItemAction4,
                onRenameLaunchItem = viewModel::onRenameLaunchItem,
                showRequestContactsPermissionBanner = shouldShowRequestPermissionRationale,
                onRequestContactsPermissionClick = {
                    viewModel.shouldShowRequestPermissionRationale.value = false
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                },
                showNotificationListenerPermissionBanner = !hasNotificationListenerPermission && !hasSeenRequestNotificationListenerPermissionBanner,
                onRequestNotificationListenerPermissionClick = viewModel::onRequestNotificationListenerPermissionClick,
                alignmentBottom = alignmentBottom,
                alignmentRight = alignmentRight,
                wallpaperOpacity = wallpaperOpacity,
                showNotificationsButton = showNotificationsButton,
                keyboardHack = keyboardHack,
                gridState = gridState,
            )
        }
    }


    // Back from another app: onStart is called only
    // Home from another app: onStart then onNewIntent are called
    // Home from this app: onNewIntent is called only
    // Therefore we need to reset the search query / show the keyboard in both onStart and onNewIntent

    override fun onStart() {
        super.onStart()
        viewModel.resetSearchQuery()
    }

    override fun onResume() {
        super.onResume()
        viewModel.hasNotificationListenerPermissionSignal()
        window.decorView.postDelayed(300) {
            if (!WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets).isVisible(WindowInsetsCompat.Type.ime())) {
                showKeyboardSupposedly()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.resetSearchQuery()
    }

    private fun showKeyboardSupposedly() {
        // Force showing the keyboard, supposedly.  This works 93.78% of the time.  Shout out to /r/mAndroidDev!
        WindowCompat.getInsetsController(window, window.decorView).show(WindowInsetsCompat.Type.ime())
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        viewModel.resetSearchQuery()
        showKeyboardSupposedly()
    }
}
