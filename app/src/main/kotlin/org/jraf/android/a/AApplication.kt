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
package org.jraf.android.a

import android.app.Application
import android.content.Context
import org.jraf.android.a.data.AppRepository
import org.jraf.android.a.data.ContactRepository
import org.jraf.android.a.data.LaunchItemRepository
import org.jraf.android.a.data.NotificationRepository
import org.jraf.android.a.data.ShortcutRepository
import org.jraf.android.a.util.Key
import org.jraf.android.a.util.initLogging

class AApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initLogging()

        // Material dynamic colors
//        DynamicColors.applyToActivitiesIfAvailable(this)

        initRepositories(this)
    }

    private val repositories = mutableMapOf<Key<*>, Any>()

    private fun initRepositories(context: Context) {
        repositories[AppRepository] = AppRepository(context)
        repositories[ContactRepository] = ContactRepository(context)
        repositories[LaunchItemRepository] = LaunchItemRepository(context)
        repositories[ShortcutRepository] = ShortcutRepository(context)
        repositories[NotificationRepository] = NotificationRepository(context)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: Key<T>): T = repositories[key] as T
}

val Context.app: AApplication
    get() = applicationContext as AApplication

operator fun <T> Context.get(key: Key<T>): T = app[key]
