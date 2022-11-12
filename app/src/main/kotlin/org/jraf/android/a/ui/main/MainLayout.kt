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

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package org.jraf.android.a.ui.main

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.jraf.android.a.R
import org.jraf.android.a.ui.theme.ATheme
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MainLayout(
    searchQuery: String,
    apps: List<MainViewModel.App>,
    onSearchQueryChange: (String) -> Unit,
    onResetSearchQueryClick: () -> Unit,
    onWebSearchClick: () -> Unit,
    onAppClick: (MainViewModel.App) -> Unit,
    onAppLongClick: (MainViewModel.App) -> Unit,
    gridState: LazyGridState,
) {
    ATheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .padding(8.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                SearchTextField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onResetSearchQueryClick = onResetSearchQueryClick,
                    onWebSearchClick = onWebSearchClick,
                )
                AppList(
                    apps = apps,
                    onAppClick = onAppClick,
                    onAppLongClick = onAppLongClick,
                    gridState = gridState
                )
            }
        }
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResetSearchQueryClick: () -> Unit,
    onWebSearchClick: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // No comment...
        delay(2000)
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(8.dp),
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(text = stringResource(R.string.main_search_placeholder))
        },
        trailingIcon = {
            Crossfade(searchQuery.isNotBlank()) { visible ->
                if (visible) {
                    Row {
                        IconButton(onClick = { onResetSearchQueryClick() }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.main_search_reset),
                            )
                        }

                        IconButton(onClick = { onWebSearchClick() }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.main_search_webSearch),
                            )
                        }
                    }
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Search,
            autoCorrect = false,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onWebSearchClick() },
        ),
    )
}

@Composable
private fun AppList(
    apps: List<MainViewModel.App>,
    onAppClick: (MainViewModel.App) -> Unit,
    onAppLongClick: (MainViewModel.App) -> Unit,
    gridState: LazyGridState,
) {
    LazyVerticalGrid(
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxSize(),
        columns = GridCells.Fixed(5),
        state = gridState,
    ) {
        items(apps, key = { it.packageName + it.activityName }) { app ->
            AppItem(app, onAppClick, onAppLongClick)
        }
    }
}

@Composable
private fun LazyGridItemScope.AppItem(
    app: MainViewModel.App,
    onAppClick: (MainViewModel.App) -> Unit,
    onAppLongClick: (MainViewModel.App) -> Unit
) {
    Column(
        modifier = Modifier
            .animateItemPlacement()
            .combinedClickable(
                onClick = { onAppClick(app) },
                onLongClick = { onAppLongClick(app) }
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .size(48.dp),
            painter = DrawablePainter(app.drawable),
            contentDescription = app.label,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier
                .padding(horizontal = 2.dp),
            text = app.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
        )
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenPreview() {
    MainLayout(
        searchQuery = "",
        apps = listOf(
            fakeApp(),
            fakeApp(),
            fakeApp(),
            fakeApp(),
            fakeApp(),
            fakeApp(),
            fakeApp(),
            fakeApp(),
            fakeApp(),
        ),
        onSearchQueryChange = {},
        onResetSearchQueryClick = {},
        onWebSearchClick = {},
        onAppClick = {},
        onAppLongClick = {},
        gridState = rememberLazyGridState(),
    )
}

@Composable
private fun fakeApp() = MainViewModel.App(
    label = "My App" * Random.nextInt(1, 4),
    packageName = Random.nextInt().toString(),
    activityName = Random.nextInt().toString(),
    drawable = ContextCompat.getDrawable(
        LocalContext.current,
        R.mipmap.ic_launcher
    )!!
)


private operator fun String.times(times: Int): String {
    return buildString {
        for (i in 0 until times) {
            append(this@times)
            if (i < times - 1) append(" ")
        }
    }
}

private class DrawablePainter(private val drawable: Drawable) : Painter() {
    override val intrinsicSize: Size =
        Size(width = drawable.intrinsicWidth.toFloat(), height = drawable.intrinsicHeight.toFloat())

    override fun DrawScope.onDraw() {
        drawIntoCanvas { canvas ->
            // Update the Drawable's bounds
            drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
            drawable.draw(canvas.nativeCanvas)
        }

    }
}
