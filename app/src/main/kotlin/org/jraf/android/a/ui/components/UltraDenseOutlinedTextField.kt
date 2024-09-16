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
package org.jraf.android.a.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.jraf.android.a.R
import org.jraf.android.a.ui.theme.ATheme
import org.jraf.android.a.util.toDp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UltraDenseOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val mergedTextStyle = textStyle.merge(TextStyle(color = MaterialTheme.colorScheme.onSurface))
    Row(
        modifier = modifier
            .border(
                border = BorderStroke(2.sp.toDp(), SolidColor(MaterialTheme.colorScheme.primary)),
                shape = OutlinedTextFieldDefaults.shape,
            )
            .padding(horizontal = 16.sp.toDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
        ) {
            if (placeholder != null && value.isEmpty()) {
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    placeholder()
                }
            }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = mergedTextStyle,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                singleLine = singleLine,
                maxLines = maxLines,
                minLines = minLines,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            )
        }
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentEnforcement provides false,
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            trailingIcon?.invoke()
        }
    }
}

@Preview(showSystemUi = true, showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UltraDenseOutlinedTextFieldPreview() {
    ATheme {
        Surface {
            UltraDenseOutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.sp.toDp()),
                value = "",
                onValueChange = {},
                placeholder = { Text("Placeholder") },
                trailingIcon = {
                    Row {
                        IconButton(
                            modifier = Modifier.height(24.sp.toDp()),
                            onClick = { }
                        ) {
                            Icon(
                                modifier = Modifier.height(24.sp.toDp()),
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.main_search_reset),
                            )
                        }

                        IconButton(
                            modifier = Modifier.height(24.sp.toDp()),
                            onClick = { }
                        ) {
                            Icon(
                                modifier = Modifier.height(24.sp.toDp()),
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.main_search_webSearch),
                            )
                        }
                    }
                }
            )
        }
    }
}
