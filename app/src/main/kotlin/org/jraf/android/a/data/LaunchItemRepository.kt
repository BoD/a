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
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.jraf.android.a.Database

private const val LONG_TERM_HISTORY_SIZE = 300L
private const val LONG_TERM_WEIGHT = 1L

private const val SHORT_TERM_HISTORY_SIZE = 20L
private const val SHORT_TERM_WEIGHT = 3L

class LaunchItemRepository(private val context: Context) {
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

    val counters: Flow<Map<String, Long>> = run {
        val longTermCounters = getCounters(LONG_TERM_HISTORY_SIZE, LONG_TERM_WEIGHT)
        val shortTermCounters = getCounters(SHORT_TERM_HISTORY_SIZE, SHORT_TERM_WEIGHT)
        longTermCounters.combine(shortTermCounters) { longTerm, shortTerm ->
            longTerm.mapValues { it.value + shortTerm.getOrDefault(it.key, 0) }
        }
    }

    private fun getCounters(historySize: Long, weight: Long) = database.launchedItemsQueries.select(historySize = historySize)
        .asFlow()
        .mapToList()
        .map { counters ->
            counters.associate { it.id to it.count * weight }
        }
}
