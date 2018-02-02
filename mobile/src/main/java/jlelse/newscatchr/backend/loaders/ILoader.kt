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

package jlelse.newscatchr.backend.loaders

import jlelse.newscatchr.backend.Article

interface ILoader {
	var type: ILoader.FeedTypes?
	var count: Int
	var query: String?
	var feedUrl: String?
	var ranked: ILoader.Ranked
	var continuation: String?

	fun items(cache: Boolean): List<Article>?
	fun moreItems(): List<Article>?
	fun itemsByIds(ids: Array<String>?, cache: Boolean): List<Article>?

	enum class FeedTypes { FEED, SEARCH, MIX }
	enum class Ranked { NEWEST, OLDEST }
}