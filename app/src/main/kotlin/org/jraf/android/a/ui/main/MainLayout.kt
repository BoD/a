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
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import org.jraf.android.a.R
import org.jraf.android.a.ui.theme.ATheme
import org.jraf.android.a.util.toDp
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MainLayout(
    searchQuery: String,
    launchItems: List<MainViewModel.LaunchItem>,
    shouldShowRequestPermissionRationale: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onResetSearchQueryClick: () -> Unit,
    onWebSearchClick: () -> Unit,
    onKeyboardActionButtonClick: () -> Unit,
    isKeyboardWebSearchActive: Boolean,
    onLaunchItemPrimaryAction: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemSecondaryAction: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemTertiaryAction: (MainViewModel.LaunchItem) -> Unit,
    onRequestPermissionRationaleClick: () -> Unit,
    gridState: LazyGridState,
) {
    ATheme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                SearchTextField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onResetSearchQueryClick = onResetSearchQueryClick,
                    onWebSearchClick = onWebSearchClick,
                    onKeyboardActionButtonClick = onKeyboardActionButtonClick,
                    isKeyboardWebSearchActive = isKeyboardWebSearchActive,
                )
                if (shouldShowRequestPermissionRationale) {
                    RequestPermissionRationale(onRequestPermissionRationaleClick = onRequestPermissionRationaleClick)
                }

                LaunchItemList(
                    launchItems = launchItems,
                    onLaunchItemPrimaryAction = onLaunchItemPrimaryAction,
                    onLaunchItemSecondaryAction = onLaunchItemSecondaryAction,
                    onLaunchItemTertiaryAction = onLaunchItemTertiaryAction,
                    gridState = gridState
                )
            }
        }
    }
}

@Composable
private fun RequestPermissionRationale(onRequestPermissionRationaleClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    )
    {
        Text(
            modifier = Modifier
                .padding(8.sp.toDp())
                .weight(1F),
            text = stringResource(R.string.main_requestPermissionRationale_text),
        )

        Spacer(modifier = Modifier.size(8.sp.toDp()))

        Button(onClick = onRequestPermissionRationaleClick) {
            Text(text = stringResource(R.string.main_requestPermissionRationale_button))
        }
        Spacer(modifier = Modifier.size(8.sp.toDp()))
    }
}

@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onResetSearchQueryClick: () -> Unit,
    onWebSearchClick: () -> Unit,
    onKeyboardActionButtonClick: () -> Unit,
    isKeyboardWebSearchActive: Boolean,
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
            .padding(top = 8.sp.toDp(), start = 8.sp.toDp(), end = 8.sp.toDp()),
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(text = stringResource(R.string.main_search_placeholder))
        },
        trailingIcon = {
            Crossfade(searchQuery.isNotBlank(), label = "trailingIconCrossFade") { visible ->
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
            imeAction = if (isKeyboardWebSearchActive) ImeAction.Search else ImeAction.Go,
            autoCorrect = false,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onKeyboardActionButtonClick() },
            onGo = { onKeyboardActionButtonClick() },
        ),
    )
}

@Composable
private fun LaunchItemList(
    launchItems: List<MainViewModel.LaunchItem>,
    onLaunchItemPrimaryAction: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemSecondaryAction: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemTertiaryAction: (MainViewModel.LaunchItem) -> Unit,
    gridState: LazyGridState,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.sp.toDp()),
            columns = GridCells.Adaptive(minSize = 64.sp.toDp()),
            state = gridState,
        ) {
            items(launchItems, key = { it.id }) { launchItem ->
                LaunchItemItem(
                    launchItem = launchItem,
                    onLaunchItemPrimaryAction = onLaunchItemPrimaryAction,
                    onLaunchItemSecondaryAction = onLaunchItemSecondaryAction,
                    onLaunchItemTertiaryAction = onLaunchItemTertiaryAction,
                )
            }
        }

        // Fading edge
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(12.sp.toDp())
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = .8f),
                            Color.Transparent,
                        )
                    )
                )
        )
    }
}

private val deprioritizedColorFilter = ColorFilter.colorMatrix(
    ColorMatrix().apply {
        setToSaturation(0F)
    }
)

@Composable
private fun LazyGridItemScope.LaunchItemItem(
    launchItem: MainViewModel.LaunchItem,
    onLaunchItemPrimaryAction: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemSecondaryAction: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemTertiaryAction: (MainViewModel.LaunchItem) -> Unit,
) {
    var dropdownMenuVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .animateItemPlacement()
            .padding(vertical = 6.sp.toDp()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(
                    onClick = { onLaunchItemPrimaryAction(launchItem) },
                    onLongClick = {
                        if (launchItem is MainViewModel.AppLaunchItem) {
                            dropdownMenuVisible = true
                        } else {
                            onLaunchItemSecondaryAction(launchItem)
                        }
                    }
                )
                .let {
                    if (launchItem.isDeprioritized) {
                        it.alpha(.5f)
                    } else {
                        it
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier
                    .size(48.sp.toDp())
                    .let {
                        if (launchItem is MainViewModel.ContactLaunchItem) {
                            // Contact photos are square, but we want circles
                            it.clip(CircleShape)
                        } else {
                            it
                        }
                    },
                painter = DrawablePainter(launchItem.drawable),
                contentDescription = launchItem.label,
                colorFilter = if (launchItem.isDeprioritized) {
                    deprioritizedColorFilter
                } else {
                    null
                },
            )
            Spacer(modifier = Modifier.height(4.sp.toDp()))
            Text(
                modifier = Modifier.padding(horizontal = 2.sp.toDp()),
                text = launchItem.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
            )
        }

        if (launchItem is MainViewModel.AppLaunchItem) {
            DropdownMenu(expanded = dropdownMenuVisible, onDismissRequest = { dropdownMenuVisible = false }) {
                DropdownMenuItem(
                    onClick = {
                        onLaunchItemSecondaryAction(launchItem)
                        dropdownMenuVisible = false
                    },
                    text = { Text(stringResource(R.string.main_list_app_appDetails)) }
                )
                DropdownMenuItem(
                    onClick = {
                        onLaunchItemTertiaryAction(launchItem)
                        dropdownMenuVisible = false
                    },
                    text = {
                        Text(
                            stringResource(
                                if (launchItem.isDeprioritized) {
                                    R.string.main_list_app_undeprioritize
                                } else {
                                    R.string.main_list_app_deprioritize
                                }
                            )
                        )
                    }
                )
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainScreenPreview() {
    MainLayout(
        searchQuery = "",
        launchItems = listOf(
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
        shouldShowRequestPermissionRationale = false,
        onSearchQueryChange = {},
        onResetSearchQueryClick = {},
        onWebSearchClick = {},
        onKeyboardActionButtonClick = {},
        isKeyboardWebSearchActive = false,
        onLaunchItemPrimaryAction = {},
        onLaunchItemSecondaryAction = {},
        onLaunchItemTertiaryAction = {},
        onRequestPermissionRationaleClick = {},
        gridState = rememberLazyGridState(),
    )
}

@Composable
private fun fakeApp() = MainViewModel.AppLaunchItem(
    label = "My App" * Random.nextInt(1, 4),
    packageName = Random.nextInt().toString(),
    activityName = Random.nextInt().toString(),
    drawable = ContextCompat.getDrawable(
        LocalContext.current,
        R.mipmap.ic_launcher
    )!!,
    isDeprioritized = false
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
