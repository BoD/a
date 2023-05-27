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

package org.jraf.android.a.ui.main

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jraf.android.a.ui.main.MainViewModel.LaunchItem

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        lifecycleScope.launch {
            viewModel.intentToStart.collect { intent ->
                startActivity(intent)
            }
        }

        setContent {
            val searchQuery: String by viewModel.searchQuery.collectAsState(initial = "")
            val launchItems: List<LaunchItem> by viewModel.filteredLaunchItems.collectAsState(initial = emptyList())
            val isKeyboardWebSearchActive: Boolean by viewModel.isKeyboardWebSearchActive.collectAsState(
                initial = false
            )
            val scrollUp: Any by viewModel.scrollUp.collectAsState(initial = Unit)

            val gridState = rememberLazyGridState()

            LaunchedEffect(scrollUp) {
                delay(225)
                gridState.animateScrollToItem(0)
            }

            MainLayout(
                searchQuery = searchQuery,
                launchItems = launchItems,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onResetSearchQueryClick = viewModel::resetSearchQuery,
                onWebSearchClick = viewModel::onWebSearchClick,
                onKeyboardActionButtonClick = viewModel::onKeyboardActionButtonClick,
                isKeyboardWebSearchActive = isKeyboardWebSearchActive,
                onLaunchItemClick = viewModel::onLaunchItemClick,
                onLaunchItemLongClick = viewModel::onLaunchItemLongClick,
                gridState = gridState,
            )
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.resetSearchQuery()

        showKeyboardSupposedly()
    }

    private fun showKeyboardSupposedly() {
        // Force showing the keyboard, supposedly
        val imm: InputMethodManager = getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showKeyboardSupposedly()
    }
}
