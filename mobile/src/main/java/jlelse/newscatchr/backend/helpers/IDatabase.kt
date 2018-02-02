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

package jlelse.newscatchr.backend.helpers

import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed

interface IDatabase {
	var allFavorites: Array<Feed>
	var allBookmarks: Array<Article>
	var allReadUrls: Array<String>
	var allLastFeeds: Array<Feed>

	fun addFavorites(vararg feeds: Feed?)
	fun deleteFavorite(url: String?)
	fun updateFavoriteTitle(feedUrl: String?, newTitle: String?)
	fun isFavorite(url: String?): Boolean

	fun addBookmark(article: Article?)
	fun deleteBookmark(url: String?)
	fun isBookmark(url: String?): Boolean

	fun addReadUrl(url: String?): Unit?
	fun isReadUrl(url: String?): Boolean

	fun addLastFeed(feed: Feed?)

	fun Feed?.favorite(): Boolean
	fun Article?.bookmark(): Boolean
	fun Feed?.lastFeed(): Boolean
}