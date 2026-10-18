/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2026-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.a.di

import org.jraf.android.a.data.AppRepository
import org.jraf.android.a.data.AppRepositoryImpl
import org.jraf.android.a.data.ContactRepository
import org.jraf.android.a.data.LaunchItemRepository
import org.jraf.android.a.data.NotificationRepository
import org.jraf.android.a.data.SettingsRepository
import org.jraf.android.a.data.ShortcutRepository
import org.jraf.android.a.ui.main.MainViewModel
import org.jraf.android.a.ui.settings.SettingsViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel

val appModule = module {
    single<AppRepositoryImpl>() bind AppRepository::class
    single<ContactRepository>()
    single<LaunchItemRepository>()
    single<NotificationRepository>()
    single<SettingsRepository>()
    single<ShortcutRepository>()

    viewModel<MainViewModel>()
    viewModel<SettingsViewModel>()
}
