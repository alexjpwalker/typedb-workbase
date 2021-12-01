/*
 * Copyright (C) 2021 Vaticle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.vaticle.typedb.studio.view.common.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vaticle.typedb.studio.state.view.CatalogueItem
import com.vaticle.typedb.studio.view.common.component.Form.Text
import com.vaticle.typedb.studio.view.common.theme.Theme
import com.vaticle.typedb.studio.view.common.theme.Theme.toDP

object Catalogue {

    private val ITEM_HEIGHT = 26.dp
    private val ICON_WIDTH = 20.dp
    private val ICON_SPACING = 4.dp
    private val AREA_PADDING = 8.dp

    data class IconArgs(val code: Icon.Code, val color: @Composable () -> Color = { Theme.colors.icon })

    private class CatalogueState {
        var minWidth by mutableStateOf(0.dp)
    }

    @Composable
    fun <T : CatalogueItem<T>> Layout(items: List<T>, iconArgs: (T) -> IconArgs, itemHeight: Dp = ITEM_HEIGHT) {
        val density = LocalDensity.current.density
        val state = remember { CatalogueState() }
        Box(
            modifier = Modifier.fillMaxSize()
                .onSizeChanged { state.minWidth = toDP(it.width, density) }
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
        ) { NestedCatalogue(0, items, iconArgs, itemHeight, state) }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    private fun <T : CatalogueItem<T>> NestedCatalogue(
        depth: Int,
        items: List<T>,
        iconArgs: (T) -> IconArgs,
        itemHeight: Dp,
        state: CatalogueState,
    ) {
        val density = LocalDensity.current.density

        fun increaseToAtLeast(widthSize: Int) {
            val newWidth = toDP(widthSize, density)
            if (newWidth > state.minWidth) state.minWidth = newWidth
        }

        Column(modifier = Modifier.widthIn(min = state.minWidth).onSizeChanged { increaseToAtLeast(it.width) }) {
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.widthIn(min = state.minWidth).height(itemHeight)
                        .pointerHoverIcon(PointerIconDefaults.Hand)
                        .onSizeChanged { increaseToAtLeast(it.width) }
                        .clickable { }
                ) {
                    if (depth > 0) Spacer(modifier = Modifier.width(ICON_WIDTH * depth))
                    ExpandOrCollapseOrNoButton(item)
                    Icon(item, iconArgs)
                    Spacer(Modifier.width(ICON_SPACING))
                    Text(value = item.name, modifier = Modifier.height(ICON_WIDTH).offset(y = (-1).dp))
                    Spacer(modifier = Modifier.width(AREA_PADDING))
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (item.isExpandable && item.asExpandable().isExpanded) {
                    NestedCatalogue(depth + 1, item.asExpandable().children, iconArgs, itemHeight, state)
                }
            }
        }
    }

    @Composable
    private fun <T : CatalogueItem<T>> ExpandOrCollapseOrNoButton(item: CatalogueItem<T>) {
        if (item.isExpandable) Form.IconButton(
            icon = if (item.asExpandable().isExpanded) Icon.Code.CHEVRON_DOWN else Icon.Code.CHEVRON_RIGHT,
            onClick = { item.asExpandable().toggle() },
            bgColor = Color.Transparent,
            modifier = Modifier.size(ICON_WIDTH)
        )
        else Spacer(Modifier.size(ICON_WIDTH))
    }

    @Composable
    private fun <T : CatalogueItem<T>> Icon(item: T, iconArgs: (T) -> IconArgs) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(ICON_WIDTH)) {
            Icon.Render(icon = iconArgs(item).code, color = iconArgs(item).color())
        }
    }
}