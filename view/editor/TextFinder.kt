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

package com.vaticle.typedb.studio.view.editor

import com.vaticle.typedb.studio.state.project.File

internal class TextFinder(val file: File) {

    internal val status: String get() = "11 / 23462" // TODO

    internal fun findText(text: String, isCaseSensitive: Boolean) {
        println("findText() -> isCaseSensitive: $isCaseSensitive, text: $text")
        // TODO
    }

    internal fun findRegex(regex: Regex, isCaseSensitive: Boolean) {
        println("findRegex() -> isCaseSensitive: $isCaseSensitive, regex: $regex")
        // TODO
    }

    internal fun findNext() {
        println("findNext()")
        // TODO
    }

    internal fun findPrevious() {
        println("findPrevious()")
        // TODO
    }

    internal fun replaceNext(text: String) {
        println("replaceNext() -> text: $text")
        // TODO
    }

    internal fun replaceAll(text: String) {
        println("replaceAll() -> text: $text")
        // TODO
    }
}