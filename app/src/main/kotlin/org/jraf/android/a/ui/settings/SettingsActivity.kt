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

package org.jraf.android.a.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class SettingsActivity : ComponentActivity() {
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val alignmentBottom: Boolean by viewModel.alignmentBottom.collectAsState()
            val rightHanded: Boolean by viewModel.alignmentRight.collectAsState()
            val wallpaperOpacity: Float by viewModel.wallpaperOpacity.collectAsState()
            val showNotificationsButton: Boolean by viewModel.showNotificationsButton.collectAsState()
            val keyboardHack: Boolean by viewModel.keyboardHack.collectAsState()
            SettingsLayout(
                onNavigateBack = onBackPressedDispatcher::onBackPressed,
                alignmentBottom = alignmentBottom,
                onAlignmentBottomClick = viewModel::toggleAlignmentBottom,
                rightHanded = rightHanded,
                onAlignmentRightClick = viewModel::toggleAlignmentRight,
                wallpaperOpacity = wallpaperOpacity,
                onWallpaperOpacityChange = viewModel::setWallpaperOpacity,
                showNotificationsButton = showNotificationsButton,
                onShowNotificationsButtonClick = viewModel::toggleShowNotificationsButton,
                keyboardHack = keyboardHack,
                onKeyboardHackClick = viewModel::toggleKeyboardHack,
            )
        }
    }
}
