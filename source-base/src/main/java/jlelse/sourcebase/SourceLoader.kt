/*
 * NewsCatchr
 * Copyright © 2018 Jan-Lukas Else
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

package jlelse.sourcebase

import android.content.Context

abstract class SourceLoader {
	var type: SourceLoader.FeedTypes? = null
	var count = 20
	var query: String? = null
	var feedUrl: String? = null
	var ranked: SourceLoader.Ranked = SourceLoader.Ranked.NEWEST
	var continuation: String? = null

	abstract fun items(context: Context, cache: Boolean = true): List<Article>?
	abstract fun moreItems(context: Context): List<Article>?

	enum class FeedTypes { FEED, SEARCH, MIX }
	enum class Ranked { NEWEST, OLDEST }
}