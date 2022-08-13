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
package org.jraf.android.a.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jraf.android.a.util.mergeReduce
import java.io.InputStream
import java.io.OutputStream

private const val LONG_LAUNCH_COUNT = 300
private const val SHORT_LAUNCH_COUNT = 20

private const val RECENT_LAUNCH_WEIGHT = 3

class Data(context: Context) {
    private val settingsDataStore = DataStoreFactory.create(
        serializer = SettingsSerializer,
        produceFile = { context.dataStoreFile("settings.dataStore") },
    )

    val counters: Flow<Map<String, Int>> = settingsDataStore.data.map { settings ->
        val longActivityCounters =
            settings.longLaunchedActivityList.associateWith { activityName ->
                settings.longLaunchedActivityList.count { it == activityName }
            }
        val shortActivityCounters =
            settings.shortLaunchedActivityList.associateWith { activityName ->
                settings.shortLaunchedActivityList.count { it == activityName } * RECENT_LAUNCH_WEIGHT
            }

        longActivityCounters.mergeReduce(shortActivityCounters) { a, b -> a + b }
    }

    suspend fun incrementCounter(app: String) {
        settingsDataStore.updateData { settings ->
            val longActivityList = settings.longLaunchedActivityList.toMutableList()
            if (longActivityList.size >= LONG_LAUNCH_COUNT) {
                longActivityList.removeAt(0)
            }
            longActivityList.add(app)

            val shortActivityList = settings.shortLaunchedActivityList.toMutableList()
            if (shortActivityList.size >= SHORT_LAUNCH_COUNT) {
                shortActivityList.removeAt(0)
            }
            shortActivityList.add(app)

            settings.copy(
                longLaunchedActivityList = longActivityList,
                shortLaunchedActivityList = shortActivityList,
            )
        }
    }
}

private object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings
        get() {
            return Settings()
        }

    override suspend fun readFrom(input: InputStream): Settings {
        try {
            return Settings.ADAPTER.decode(input)
        } catch (e: Exception) {
            throw CorruptionException("Could not decode input", e)
        }
    }

    override suspend fun writeTo(
        t: Settings,
        output: OutputStream
    ) = Settings.ADAPTER.encode(output, t)
}

