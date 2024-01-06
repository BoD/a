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
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import org.jraf.android.a.app
import org.jraf.android.a.data.NotificationRepository
import org.jraf.android.a.util.logd

class NotificationListenerService : NotificationListenerService() {
    companion object {
        private val ignoredPackages = setOf(
            "com.google.android.deskclock",
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        updateActiveNotifications(null)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification, rankingMap: RankingMap) {
        super.onNotificationPosted(sbn, rankingMap)
        logd("onNotificationPosted")
        updateActiveNotifications(rankingMap)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        logd("onNotificationRemoved")
        updateActiveNotifications(rankingMap)
    }

    override fun onNotificationRankingUpdate(rankingMap: RankingMap) {
        super.onNotificationRankingUpdate(rankingMap)
        logd("onNotificationRankingUpdate")
        updateActiveNotifications(rankingMap)
    }

    private fun updateActiveNotifications(rankingMap: RankingMap?) {
        val currentRankingMap = rankingMap ?: currentRanking
        val rankingByKey: Map<String, Ranking> = currentRankingMap.let {
            it.orderedKeys.associateWith { key ->
                val ranking = Ranking()
                it.getRanking(key, ranking)
                ranking
            }
        }

        val activeNotifications = activeNotifications

        // Don't consider ignored packages, media, and ongoing notifications
        val filteredNotifications: List<StatusBarNotification> = activeNotifications
            .filter { statusBarNotification ->
                val hasMediaSession = statusBarNotification.notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION)
                val isIgnoredPackage = statusBarNotification.packageName in ignoredPackages
                !isIgnoredPackage &&
                        !statusBarNotification.isOngoing &&
                        statusBarNotification.isClearable &&
                        !hasMediaSession
            }

        // Group notifications by group key
        val groupedNotifications: Map<String, List<StatusBarNotification>> = filteredNotifications
            .groupBy { statusBarNotification -> statusBarNotification.groupKey }

        // Keep only the summary notification for each group, and mark it as a conversation if any of the notifications in the group is a conversation
        val summaryNotificationsWithConversation: List<Pair<StatusBarNotification, Boolean>> = groupedNotifications
            .map { (_, notifications) ->
                val isConversation = notifications.any { statusBarNotification ->
                    rankingByKey[statusBarNotification.key]?.conversation == true
                }
                (notifications.firstOrNull { statusBarNotification ->
                    statusBarNotification.notification.flags and Notification.FLAG_GROUP_SUMMARY == Notification.FLAG_GROUP_SUMMARY
                } ?: notifications.first()) to isConversation
            }

        val summaryNotificationsSortedByRanking: List<StatusBarNotification> = summaryNotificationsWithConversation
            .sortedWith { (aNotification, aIsConversation), (bNotification, bIsConversation) ->
                val aRanking = rankingByKey[aNotification.key]!!
                val bRanking = rankingByKey[bNotification.key]!!
                if (aIsConversation) {
                    if (bIsConversation) {
                        // Both are conversations, sort by ranking
                        aRanking.rank.compareTo(bRanking.rank)
                    } else {
                        // Only A is a conversation, A comes first
                        -1
                    }
                } else {
                    if (bIsConversation) {
                        // Only B is a conversation, B comes first
                        1
                    } else {
                        // None is a conversation, sort by ranking
                        aRanking.rank.compareTo(bRanking.rank)
                    }
                }
            }
            .map { (notification, _) -> notification }

        // Discard low importance notifications and associate by package name
        val notificationRankings = summaryNotificationsSortedByRanking
            .filter { statusBarNotification ->
                val ranking = rankingByKey[statusBarNotification.key]!!
                val isChannelLowImportance = ranking.channel.importance == NotificationManagerCompat.IMPORTANCE_LOW ||
                        ranking.channel.importance == NotificationManagerCompat.IMPORTANCE_MIN
                val isNotificationLowImportance = ranking.importance == NotificationManagerCompat.IMPORTANCE_LOW ||
                        ranking.importance == NotificationManagerCompat.IMPORTANCE_MIN
                !ranking.isAmbient &&
                        !ranking.suspended &&
                        ranking.canShowBadge() &&
                        !isNotificationLowImportance &&
                        !isChannelLowImportance
            }
            // associate() will keep the last value in case of a collision, so put the highest (well, lowest, since rank 0 is the highest rank) ranking at the end of the list
            .sortedByDescending { statusBarNotification -> summaryNotificationsSortedByRanking.indexOf(statusBarNotification) }
            .associate { statusBarNotification ->
                statusBarNotification.packageName to summaryNotificationsSortedByRanking.indexOf(statusBarNotification)
            }
        app[NotificationRepository].updateNotificationRankings(notificationRankings)
    }
}

private val NotificationListenerService.Ranking.suspended
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        isSuspended
    } else {
        false
    }


private val NotificationListenerService.Ranking.conversation
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        isConversation
    } else {
        false
    }
