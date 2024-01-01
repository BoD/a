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
package org.jraf.android.a.notification

import android.app.Notification
import android.service.notification.StatusBarNotification
import org.jraf.android.a.app
import org.jraf.android.a.data.NotificationRepository
import org.jraf.android.a.util.logd

class NotificationListenerService : android.service.notification.NotificationListenerService() {
    companion object {
        private val ignoredPackages = setOf(
            "com.google.android.deskclock",
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        updateActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        logd("onNotificationPosted")
        updateActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap?, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        logd("onNotificationRemoved")
        updateActiveNotifications()
    }

    private fun updateActiveNotifications() {
        app[NotificationRepository].updateNotifications(
            @Suppress("DEPRECATION")
            activeNotifications.filter { statusBarNotification ->
                val notification = statusBarNotification.notification!!
                val hasMediaSession = notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
                val isOwnNotification = statusBarNotification.packageName == packageName
                val isIgnoredPackage = statusBarNotification.packageName in ignoredPackages
                val hasOverrideGroupKey = statusBarNotification.overrideGroupKey != null
                val isNotificationSilent = notification.priority == Notification.PRIORITY_LOW ||
                        notification.priority == Notification.PRIORITY_MIN

//                val isChannelSilent = getSystemService(NotificationManager::class.java)
//                    .getNotificationChannel(notification.channelId).importance.let {
//                        it == NotificationManager.IMPORTANCE_LOW || it == NotificationManager.IMPORTANCE_MIN
//                    }

                notification.channelId
                !isOwnNotification &&
                        !isIgnoredPackage &&
                        !statusBarNotification.isOngoing &&
                        statusBarNotification.isClearable &&
                        !hasOverrideGroupKey &&
                        !hasMediaSession &&
                        !(isNotificationSilent /*|| isChannelSilent*/)
            }.associateBy { it.packageName }
        )
    }
}
