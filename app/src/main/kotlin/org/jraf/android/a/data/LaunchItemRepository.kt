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
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jraf.android.a.Database
import org.jraf.android.a.util.Key

private const val LONG_TERM_HISTORY_SIZE = 600L
private const val LONG_TERM_WEIGHT = 1L

private const val SHORT_TERM_HISTORY_SIZE = 20L
private const val SHORT_TERM_WEIGHT = 3L

class LaunchItemRepository(private val context: Context) {
    companion object : Key<LaunchItemRepository>

    private val database: Database by lazy { createSqldelightDatabase(context) }

    private fun createSqldelightDatabase(context: Context): Database {
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = "a.db",
        )
        return Database(driver)
    }

    suspend fun recordLaunchedItem(id: String) {
        withContext(Dispatchers.IO) {
            database.launchedItemsQueries.insert(id)
        }
    }

    sealed interface Counter {
        data class ShortAndLongTerm(
            val longTerm: Long,
            val combined: Long,
        ) : Counter

        object Deprioritized : Counter
    }

    val counters: Flow<Map<String, Counter>> = run {
        val longTermCounters = getCounters(LONG_TERM_HISTORY_SIZE, LONG_TERM_WEIGHT)
        val shortTermCounters = getCounters(SHORT_TERM_HISTORY_SIZE, SHORT_TERM_WEIGHT)
        val deprioritizedItems = getDeprioritizedItems()
        combine(longTermCounters, shortTermCounters, deprioritizedItems) { longTerm, shortTerm, deprioritized ->
            longTerm.mapValues { Counter.ShortAndLongTerm(longTerm = it.value, combined = it.value + shortTerm.getOrDefault(it.key, 0)) } +
                    deprioritized.map { it to Counter.Deprioritized }
        }
    }

    private fun getCounters(historySize: Long, weight: Long): Flow<Map<String, Long>> =
        database.launchedItemsQueries.select(historySize = historySize)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { counters ->
                counters.associate { it.id to it.count * weight }
            }

    suspend fun deprioritizeItem(id: String) {
        withContext(Dispatchers.IO) {
            database.deprioritizedItemsQueries.insert(id)
            // Also reset counter
            database.launchedItemsQueries.delete(id)
        }
    }

    suspend fun undeprioritizeItem(id: String) {
        withContext(Dispatchers.IO) {
            database.deprioritizedItemsQueries.delete(id)
        }
    }

    private fun getDeprioritizedItems(): Flow<List<String>> = database.deprioritizedItemsQueries.select()
        .asFlow()
        .mapToList(Dispatchers.IO)

    suspend fun ignoreNotifications(id: String) {
        withContext(Dispatchers.IO) {
            database.ignoredNotificationsItemsQueries.insert(id)
        }
    }

    suspend fun unignoreNotifications(id: String) {
        withContext(Dispatchers.IO) {
            database.ignoredNotificationsItemsQueries.delete(id)
        }
    }

    fun getIgnoredNotificationsItems(): Flow<List<String>> = database.ignoredNotificationsItemsQueries.select()
        .asFlow()
        .mapToList(Dispatchers.IO)

    suspend fun deleteItem(id: String) {
        withContext(Dispatchers.IO) {
            database.deletedItemsQueries.insert(id)
            // Also reset counter
            database.launchedItemsQueries.delete(id)
        }
    }

    fun getDeletedItems(): Flow<List<String>> = database.deletedItemsQueries.select()
        .asFlow()
        .mapToList(Dispatchers.IO)

    fun getRenamedItems(): Flow<Map<String, String>> = database.renamedItemsQueries.select()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { it.associate { renamedItem -> renamedItem.id to renamedItem.label } }

    suspend fun renameItem(id: String, label: String) {
        withContext(Dispatchers.IO) {
            database.renamedItemsQueries.insert(id, label)
        }
    }

    suspend fun unrenameItem(id: String) {
        withContext(Dispatchers.IO) {
            database.renamedItemsQueries.delete(id)
        }
    }
}
