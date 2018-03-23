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

import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.url
import jlelse.sourcebase.Article

interface IDatabase {
	var allFavorites: MutableList<Feed>
	var allBookmarks: MutableList<Article>
	var allReadUrls: MutableList<String>
	var allLastFeeds: MutableList<Feed>

	fun addFavorites(vararg feeds: Feed?)
	fun deleteFavorite(url: String?)
	fun updateFavoriteTitle(feedUrl: String?, newTitle: String?)
	fun swapFavorites(position1: Int, position2: Int)
	fun isFavorite(url: String?): Boolean

	fun addBookmark(article: Article?)
	fun deleteBookmark(url: String?)
	fun isBookmark(url: String?): Boolean

	fun addReadUrl(url: String?)
	fun isReadUrl(url: String?): Boolean

	fun addLastFeed(feed: Feed?)
	fun deleteAllLastFeeds()

	fun Feed?.safeFavorite(): Boolean = this != null && !this.url().isNullOrBlank()
	fun Article?.safeBookmark(): Boolean = this != null && !this.link.isNullOrBlank()
	fun Feed?.safeLastFeed(): Boolean = this != null && !this.url().isNullOrBlank()
}