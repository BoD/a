/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2026-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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

import android.content.ComponentName
import android.os.Process
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.jraf.android.a.R
import org.jraf.android.a.ui.components.TAG_TEXT_FIELD
import org.jraf.android.a.ui.main.MainViewModel.AppLaunchItem
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Given the launcher with some apps,
     * When the user types some text,
     * Then the query callback is called.
     */
    @Test
    fun testMainLayoutQueryChangeCallback() {
        var query = ""
        composeTestRule.setContent {

            // Given the launcher with some apps,
            MainLayout(
                searchQuery = "",
                hasNotifications = true,
                launchItems = listOf(
                    fakeApp(
                        label = "Fake Contacts",
                        pkg = "com.acme.contacts",
                        cls = "com.acme.contacts.MainActivity",
                    ),
                    fakeApp(
                        label = "Fake Phone",
                        pkg = "com.acme.phone",
                        cls = "com.acme.phone.MainActivity",
                    ),
                    fakeApp(
                        label = "Fake Minesweeper",
                        pkg = "com.acme.minesweeper",
                        cls = "com.acme.minesweeper.MainActivity",
                    ),
                ),
                onSearchQueryChange = { query = it },
                onResetSearchQueryClick = {},
                onWebSearchClick = {},
                onKeyboardActionButtonClick = {},
                isKeyboardWebSearchActive = false,
                onLaunchItemAction1 = {},
                onLaunchItemAction2 = {},
                onLaunchItemAction3 = {},
                onLaunchItemAction4 = {},
                onRenameLaunchItem = { _, _ -> },
                showRequestContactsPermissionBanner = false,
                onRequestContactsPermissionClick = {},
                showNotificationListenerPermissionBanner = true,
                onRequestNotificationListenerPermissionClick = {},
                alignmentBottom = true,
                alignmentRight = false,
                wallpaperOpacity = .10F,
                showNotificationsButton = true,
                keyboardHack = true,
                gridState = rememberLazyGridState(),
            )
        }

        // When the user types some text,
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TAG_TEXT_FIELD).performTextInput("Cont")

        // Then the query callback is called.
        assertEquals("Cont", query)
    }

    private fun fakeApp(
        label: String,
        pkg: String,
        cls: String,
    ) = AppLaunchItem(
        label = label,
        drawable = ContextCompat.getDrawable(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.mipmap.ic_launcher,
        )!!,
        isDeprioritized = false,
        notificationRanking = 42,
        isRenamed = false,
        ignoreNotifications = false,
        componentName = ComponentName(pkg, cls),
        user = Process.myUserHandle(),
        isPrivateSpaceLocked = false,
    )
}