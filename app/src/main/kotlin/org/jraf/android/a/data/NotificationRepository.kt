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
import android.os.UserHandle
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.jraf.android.a.util.Key

class NotificationRepository(private val context: Context) {
    companion object : Key<NotificationRepository>

    private val _notificationRankings: MutableStateFlow<Map<NotificationKey, Int>> = MutableStateFlow(emptyMap())
    val notificationRankings: Flow<Map<NotificationKey, Int>> = _notificationRankings

    fun updateNotificationRankings(notificationRankings: Map<NotificationKey, Int>) {
        _notificationRankings.value = notificationRankings
    }

    fun hasNotificationListenerPermission(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

    data class NotificationKey(
        val packageName: String,
        val user: UserHandle,
    )
}
