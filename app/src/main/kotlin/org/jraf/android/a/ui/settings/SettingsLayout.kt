/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2024-present Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.android.a.ui.settings

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jraf.android.a.R
import org.jraf.android.a.ui.theme.ATheme
import org.jraf.android.a.util.toDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsLayout(
    alignmentBottom: Boolean,
    rightHanded: Boolean,
    onAlignmentBottomClick: () -> Unit,
    onAlignmentRightClick: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    ATheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateBack()
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null
                            )
                        }
                    },
                    title = { Text(stringResource(id = R.string.settings_title)) }
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                SwitchSetting(
                    value = alignmentBottom,
                    onClick = onAlignmentBottomClick,
                    titleResId = R.string.settings_alignmentBottom_title,
                    trueLabelResId = R.string.settings_alignmentBottom_bottom,
                    falseLabelResId = R.string.settings_alignmentBottom_top,
                )
                SwitchSetting(
                    value = rightHanded,
                    onClick = onAlignmentRightClick,
                    titleResId = R.string.settings_alignmentRight_title,
                    trueLabelResId = R.string.settings_alignmentRight_right,
                    falseLabelResId = R.string.settings_alignmentRight_left
                )
                GridPreview(
                    alignmentBottom = alignmentBottom,
                    alignmentRight = rightHanded,
                )
            }
        }
    }
}

@Composable
private fun SwitchSetting(
    value: Boolean,
    onClick: () -> Unit,
    @StringRes titleResId: Int,
    @StringRes trueLabelResId: Int,
    @StringRes falseLabelResId: Int,
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = {
                onClick()
            })
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1F),
            text = stringResource(titleResId),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(
            modifier = Modifier.width(16.dp)
        )
        Text(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.sp.toDp(), horizontal = 16.sp.toDp()),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            text = stringResource(if (value) trueLabelResId else falseLabelResId),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun GridPreview(alignmentBottom: Boolean, alignmentRight: Boolean) {
    CompositionLocalProvider(LocalLayoutDirection provides if (alignmentRight) LayoutDirection.Rtl else LayoutDirection.Ltr) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            contentPadding = if (alignmentBottom) PaddingValues(bottom = 2.sp.toDp()) else PaddingValues(top = 2.sp.toDp()),
            columns = GridCells.Adaptive(minSize = 64.sp.toDp()),
            reverseLayout = alignmentBottom,
        ) {
            for (i in 1..15) {
                item {
                    PreviewItem(id = i)
                }
            }
        }
    }
}

@Composable
private fun PreviewItem(id: Int) {
    Box(
        modifier = Modifier.padding(vertical = 6.sp.toDp()),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(48.sp.toDp())) {
                Text(
                    modifier = Modifier
                        .size(48.sp.toDp())
                        .clip(CircleShape)
                        .background(colorResource(R.color.ic_launcher_background).copy(alpha = 1F - id / 18F))
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    text = id.toString(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.height(4.sp.toDp()))
            Text(
                modifier = Modifier.padding(horizontal = 2.sp.toDp()),
                text = "My app",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsLayoutPreview() {
    SettingsLayout(
        alignmentBottom = true,
        rightHanded = true,
        onAlignmentBottomClick = {},
        onAlignmentRightClick = {},
        onNavigateBack = {},
    )
}
