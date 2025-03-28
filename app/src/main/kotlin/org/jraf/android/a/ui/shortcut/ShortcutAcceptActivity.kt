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

package org.jraf.android.a.ui.shortcut

import android.content.pm.LauncherApps
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.jraf.android.a.R
import org.jraf.android.a.app
import org.jraf.android.a.data.LaunchItemRepository
import org.jraf.android.a.data.ShortcutRepository

class ShortcutAcceptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launcherApps = getSystemService(LauncherApps::class.java)
        val pinItemRequest = launcherApps.getPinItemRequest(intent)
        lifecycleScope.launch {
            val launchItemRepository = app[LaunchItemRepository]
            if (launchItemRepository.isShortcutDeleted(pinItemRequest.shortcutInfo!!.id)) {
                launchItemRepository.undeleteShortcut(pinItemRequest.shortcutInfo!!.id)
            } else {
                pinItemRequest.accept()
            }
        }
        app[ShortcutRepository].notifyShortcutsChanged()
        Toast.makeText(this, getString(R.string.shortcutAccept_toast_accepted), Toast.LENGTH_SHORT).show()
        finish()
    }
}
