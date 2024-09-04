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

@file:OptIn(ExperimentalFoundationApi::class)

package org.jraf.android.a.ui.main

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.android.awaitFrame
import org.jraf.android.a.R
import org.jraf.android.a.ui.theme.ATheme
import org.jraf.android.a.util.fadingEdges
import org.jraf.android.a.util.keyboardAsState
import org.jraf.android.a.util.toDp
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun MainLayout(
    searchQuery: String,
    launchItems: List<MainViewModel.LaunchItem>,
    onSearchQueryChange: (String) -> Unit,
    onResetSearchQueryClick: () -> Unit,
    onWebSearchClick: () -> Unit,
    onKeyboardActionButtonClick: () -> Unit,
    isKeyboardWebSearchActive: Boolean,
    onLaunchItemAction1: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction2: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction3: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction4: (MainViewModel.LaunchItem) -> Unit,
    onRenameLaunchItem: (MainViewModel.LaunchItem, label: String?) -> Unit,
    showRequestContactsPermissionBanner: Boolean,
    onRequestContactsPermissionClick: () -> Unit,
    showNotificationListenerPermissionBanner: Boolean,
    onRequestNotificationListenerPermissionClick: () -> Unit,
    alignmentBottom: Boolean,
    alignmentRight: Boolean,
    wallpaperOpacity: Float,
    gridState: LazyGridState,
) {
    ATheme {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 1F - wallpaperOpacity),
            contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                var dropdownMenuVisible by remember { mutableStateOf(false) }
                val onDropdownMenuVisible: (Boolean) -> Unit = { visible ->
                    dropdownMenuVisible = visible
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (alignmentBottom) {
                        LaunchItemList(
                            launchItems = launchItems,
                            onLaunchItemAction1 = onLaunchItemAction1,
                            onLaunchItemAction2 = onLaunchItemAction2,
                            onLaunchItemAction3 = onLaunchItemAction3,
                            onLaunchItemAction4 = onLaunchItemAction4,
                            onRenameLaunchItem = onRenameLaunchItem,
                            onDropdownMenuVisible = onDropdownMenuVisible,
                            alignmentBottom = alignmentBottom,
                            alignmentRight = alignmentRight,
                            gridState = gridState,
                        )
                    }
                    SearchTextField(
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onResetSearchQueryClick = onResetSearchQueryClick,
                        onWebSearchClick = onWebSearchClick,
                        onKeyboardActionButtonClick = onKeyboardActionButtonClick,
                        isKeyboardWebSearchActive = isKeyboardWebSearchActive,
                    )
                    if (showRequestContactsPermissionBanner) {
                        RequestPermissionBanner(
                            messageResId = R.string.main_requestContactsPermissionRationale_text,
                            onRequestPermissionClick = onRequestContactsPermissionClick
                        )
                    }
                    if (showNotificationListenerPermissionBanner) {
                        RequestPermissionBanner(
                            messageResId = R.string.main_requestNotificationListenerPermission_text,
                            onRequestPermissionClick = onRequestNotificationListenerPermissionClick,
                        )
                    }
                    if (!alignmentBottom) {
                        LaunchItemList(
                            launchItems = launchItems,
                            onLaunchItemAction1 = onLaunchItemAction1,
                            onLaunchItemAction2 = onLaunchItemAction2,
                            onLaunchItemAction3 = onLaunchItemAction3,
                            onLaunchItemAction4 = onLaunchItemAction4,
                            onRenameLaunchItem = onRenameLaunchItem,
                            onDropdownMenuVisible = onDropdownMenuVisible,
                            alignmentBottom = alignmentBottom,
                            alignmentRight = alignmentRight,
                            gridState = gridState,
                        )
                    }
                }

                // XXX Hack
                // We need to pass focusable = false to DropdownMenu because otherwise it will steal the focus,
                // which will close the keyboard, which will shift the whole grid down, and the DropdownMenu will
                // appear a lot lower than expected.
                // But when focusable = false, the DropdownMenu will not close when clicking outside of it, or
                // when clicking the back button.
                // So we need to manually close it when clicking "outside of it", which is done by adding
                // this spacer under the whole grid, and intercepting the click on it, and closing the DropdownMenu.
                // We also need to detect when the keyboard closes (see isKeyboardOpen below).
                // Thanks, Compose!
                if (dropdownMenuVisible) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                dropdownMenuVisible = false
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestPermissionBanner(
    @StringRes messageResId: Int,
    onRequestPermissionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(8.sp.toDp())
                .weight(1F),
            text = stringResource(messageResId),
        )

        Spacer(modifier = Modifier.size(8.sp.toDp()))

        Button(onClick = onRequestPermissionClick) {
            Text(text = stringResource(R.string.main_requestPermissionRationale_button_ok))
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
//        delay(1800)
        awaitFrame()
        focusRequester.requestFocus()
    }

    UltraDenseOutlinedTextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .height(32.sp.toDp())
            .padding(start = 8.sp.toDp(), end = 8.sp.toDp()),
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
                        IconButton(
                            modifier = Modifier.height(32.sp.toDp()),
                            onClick = { onResetSearchQueryClick() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.main_search_reset),
                            )
                        }

                        IconButton(
                            modifier = Modifier.height(32.sp.toDp()),
                            onClick = { onWebSearchClick() }
                        ) {
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
            keyboardType = if (isKeyboardWebSearchActive) KeyboardType.Text else KeyboardType.Password,
            imeAction = if (isKeyboardWebSearchActive) ImeAction.Search else ImeAction.Go,
            autoCorrect = isKeyboardWebSearchActive,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onKeyboardActionButtonClick() },
            onGo = { onKeyboardActionButtonClick() },
        ),
    )
}

@Composable
private fun ColumnScope.LaunchItemList(
    launchItems: List<MainViewModel.LaunchItem>,
    onLaunchItemAction1: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction2: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction3: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction4: (MainViewModel.LaunchItem) -> Unit,
    onRenameLaunchItem: (MainViewModel.LaunchItem, label: String?) -> Unit,
    onDropdownMenuVisible: (Boolean) -> Unit,
    alignmentBottom: Boolean,
    alignmentRight: Boolean,
    gridState: LazyGridState,
) {
    Box(
        modifier = Modifier
            .fadingEdges()
            .fillMaxWidth()
            .weight(1F)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides if (alignmentRight) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                contentPadding = if (alignmentBottom) PaddingValues(bottom = 2.sp.toDp()) else PaddingValues(top = 2.sp.toDp()),
                columns = GridCells.Adaptive(minSize = 64.sp.toDp()),
                state = gridState,
                reverseLayout = alignmentBottom,
            ) {
                items(launchItems, key = { it.id }) { launchItem ->
                    LaunchItemItem(
                        launchItem = launchItem,
                        onLaunchItemAction1 = onLaunchItemAction1,
                        onLaunchItemAction2 = onLaunchItemAction2,
                        onLaunchItemAction3 = onLaunchItemAction3,
                        onLaunchItemAction4 = onLaunchItemAction4,
                        onRenameLaunchItem = onRenameLaunchItem,
                        onDropdownMenuVisible = onDropdownMenuVisible,
                    )
                }
            }
        }
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
    onLaunchItemAction1: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction2: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction3: (MainViewModel.LaunchItem) -> Unit,
    onLaunchItemAction4: (MainViewModel.LaunchItem) -> Unit,
    onRenameLaunchItem: (MainViewModel.LaunchItem, label: String?) -> Unit,
    onDropdownMenuVisible: (Boolean) -> Unit,
) {
    var dropdownMenuVisible by remember { mutableStateOf(false) }
    var renameDialogVisible by remember { mutableStateOf(false) }
    val isKeyboardOpen by keyboardAsState()
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) dropdownMenuVisible = false
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .animateItemPlacement()
                .padding(vertical = 6.sp.toDp()),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(
                            bounded = false,
                            radius = 56.sp.toDp(),
                        ),
                        onClick = { onLaunchItemAction1(launchItem) },
                        onLongClick = {
                            when (launchItem) {
                                is MainViewModel.AppLaunchItem,
                                is MainViewModel.ShortcutLaunchItem,
                                is MainViewModel.ASettingsLaunchItem,
                                    -> {
                                    dropdownMenuVisible = true
                                }

                                is MainViewModel.ContactLaunchItem -> {
                                    onLaunchItemAction2(launchItem)
                                }
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
                Box(
                    modifier = Modifier
                        .size(48.sp.toDp())
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
                    if (launchItem.hasNotification) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.sp.toDp())
                                .border(1.dp, Color.White, CircleShape)
                                .padding(1.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.Red),
                        )
                    }
                }
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

            onDropdownMenuVisible(dropdownMenuVisible)

            when (launchItem) {
                is MainViewModel.AppLaunchItem -> {
                    DropdownMenu(
                        expanded = dropdownMenuVisible,
                        onDismissRequest = { dropdownMenuVisible = false },
                        properties = PopupProperties(focusable = false),
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onLaunchItemAction2(launchItem)
                                dropdownMenuVisible = false
                            },
                            text = { Text(stringResource(R.string.main_list_app_appDetails)) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                if (launchItem.isRenamed) {
                                    onRenameLaunchItem(launchItem, null)
                                } else {
                                    renameDialogVisible = true
                                }
                                dropdownMenuVisible = false
                            },
                            text = {
                                Text(
                                    stringResource(
                                        if (launchItem.isRenamed) {
                                            R.string.main_list_app_unrename
                                        } else {
                                            R.string.main_list_app_rename
                                        }
                                    )
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onLaunchItemAction4(launchItem)
                                dropdownMenuVisible = false
                            },
                            text = {
                                Text(
                                    stringResource(
                                        if (launchItem.ignoreNotifications) {
                                            R.string.main_list_app_unignoreNotifications
                                        } else {
                                            R.string.main_list_app_ignoreNotifications
                                        }
                                    )
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onLaunchItemAction3(launchItem)
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

                is MainViewModel.ASettingsLaunchItem -> {
                    DropdownMenu(
                        expanded = dropdownMenuVisible,
                        onDismissRequest = { dropdownMenuVisible = false },
                        properties = PopupProperties(focusable = false),
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onLaunchItemAction2(launchItem)
                                dropdownMenuVisible = false
                            },
                            text = { Text(stringResource(R.string.main_list_app_appDetails)) }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onLaunchItemAction3(launchItem)
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

                is MainViewModel.ShortcutLaunchItem -> {
                    DropdownMenu(
                        expanded = dropdownMenuVisible,
                        onDismissRequest = { dropdownMenuVisible = false },
                        properties = PopupProperties(focusable = false),
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onLaunchItemAction2(launchItem)
                                dropdownMenuVisible = false
                            },
                            text = { Text(stringResource(R.string.main_list_shortcut_deleteShortcut)) }
                        )
                    }
                }

                is MainViewModel.ContactLaunchItem -> {}
            }
        }
    }
    if (renameDialogVisible) {
        RenameDialog(
            onDismissRequest = { renameDialogVisible = false },
            launchItem = launchItem,
            onConfirm = { label ->
                renameDialogVisible = false
                onRenameLaunchItem(launchItem, label)
            }
        )
    }
}

@Composable
private fun RenameDialog(
    onDismissRequest: () -> Unit,
    launchItem: MainViewModel.LaunchItem,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(TextFieldValue(launchItem.label, TextRange(launchItem.label.length))) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.main_list_app_rename)) },
        text = {
            val focusRequester = remember { FocusRequester() }
            OutlinedTextField(
                modifier = Modifier
                    .focusRequester(focusRequester),
                value = value,
                singleLine = true,
                onValueChange = { value = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = false,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onConfirm(value.text.trim()) },
                ),
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(value.text.trim())
                },
                enabled = value.text.isNotBlank(),
            ) {
                Text(stringResource(R.string.main_renameDialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.main_renameDialog_cancel))
            }
        }
    )
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
        onSearchQueryChange = {},
        onResetSearchQueryClick = {},
        onWebSearchClick = {},
        onKeyboardActionButtonClick = {},
        isKeyboardWebSearchActive = false,
        onLaunchItemAction1 = {},
        onLaunchItemAction2 = {},
        onLaunchItemAction3 = {},
        onLaunchItemAction4 = {},
        onRenameLaunchItem = { _, _ -> },
        showRequestContactsPermissionBanner = false,
        onRequestContactsPermissionClick = {},
        showNotificationListenerPermissionBanner = true,
        onRequestNotificationListenerPermissionClick = {},
        alignmentBottom = true,
        alignmentRight = false,
        wallpaperOpacity = .10F,
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
    isDeprioritized = false,
    notificationRanking = 42,
    isRenamed = false,
    ignoreNotifications = false,
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
