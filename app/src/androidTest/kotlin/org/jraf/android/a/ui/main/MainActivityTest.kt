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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.core.content.ContextCompat
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.jraf.android.a.R
import org.jraf.android.a.data.AppRepository
import org.jraf.android.a.fakes.data.FakeAppRepository
import org.jraf.android.a.ui.components.TAG_TEXT_FIELD
import org.jraf.android.a.ui.main.MainViewModel.AppLaunchItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltTestRule: HiltAndroidRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantContactsPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_CONTACTS)

    @Inject
    lateinit var fakeAppRepository: FakeAppRepository

    @Before
    fun setUp() {
        hiltTestRule.inject()
        fakeAppRepository.setApps(
            listOf(
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
        )
    }

    @Test
    fun testDisplaysApps() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Fake Contacts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fake Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fake Minesweeper").assertIsDisplayed()
    }

    @Test
    fun testFilterApps() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TAG_TEXT_FIELD).performTextInput("Cont")
        composeTestRule.onNodeWithText("Fake Contacts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fake Phone").assertDoesNotExist()
        composeTestRule.onNodeWithText("Fake Minesweeper").assertDoesNotExist()

    }

    private fun fakeApp(
        label: String,
        pkg: String,
        cls: String,
    ) = AppRepository.App(
        label = label,
        drawable = ContextCompat.getDrawable(
            InstrumentationRegistry.getInstrumentation().targetContext,
            R.mipmap.ic_launcher,
        )!!,
        componentName = ComponentName(pkg, cls),
        user = Process.myUserHandle(),
        isPrivateSpaceLocked = false,
    )
}