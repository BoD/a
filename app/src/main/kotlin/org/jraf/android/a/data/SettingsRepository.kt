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
import kotlinx.coroutines.flow.MutableStateFlow
import org.jraf.android.a.util.Key
import org.jraf.android.kprefs.Prefs

class SettingsRepository(context: Context) {
    companion object : Key<SettingsRepository>

    private val prefs = Prefs(context)

    val hasSeenRequestNotificationListenerPermissionBanner: MutableStateFlow<Boolean> by prefs.BooleanFlow(false)
    val alignmentBottom: MutableStateFlow<Boolean> by prefs.BooleanFlow(false, org.jraf.android.kprefs.Key("reverseLayout"))
    val alignmentRight: MutableStateFlow<Boolean> by prefs.BooleanFlow(false)
    val wallpaperOpacity: MutableStateFlow<Float> by prefs.FloatFlow(0F)
    val showNotificationsButton: MutableStateFlow<Boolean> by prefs.BooleanFlow(false)
    val keyboardHack: MutableStateFlow<Boolean> by prefs.BooleanFlow(true)
}
