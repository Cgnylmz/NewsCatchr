/*
 * NewsCatchr
 * Copyright © 2017 Jan-Lukas Else
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

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.backend.helpers

import jlelse.kos.KeyObjectStore
import jlelse.newscatchr.appContext
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.extensions.tryOrNull

/**
 * Database
 */
object Database {

	private val FAVORITES = "feeds_database"
	private val favoritesStore = KeyObjectStore(appContext!!, FAVORITES)
	private val BOOKMARKS = "bookmarks_database"
	private val bookmarksStore = KeyObjectStore(appContext!!, BOOKMARKS)
	private val READ_URLS = "urls_database"
	private val readUrlsStore = KeyObjectStore(appContext!!, READ_URLS)
	private val LAST_FEEDS = "last_feeds"
	private val lastFeedsStore = KeyObjectStore(appContext!!, LAST_FEEDS)

	private fun Feed?.safeFavorite() = this != null && !this.url().isNullOrBlank()

	var allFavorites: Array<Feed>
		get() = favoritesStore.read(FAVORITES, Array<Feed>::class.java) ?: arrayOf()
		set(value) {
			tryOrNull { favoritesStore.write<Array<Feed>>(FAVORITES, value.filter { it.safeFavorite() }.distinctBy { it.url() }.toTypedArray()) }
		}

	private val allFavoritesUrls
		get() = allFavorites.map { it.url() }

	fun addFavorites(vararg feeds: Feed?) {
		allFavorites += feeds.filterNotNull().filter { !isSavedFavorite(it.url()) }
	}

	fun deleteFavorite(url: String?) {
		allFavorites = allFavorites.filter { it.url() != url }.toTypedArray()
	}

	fun updateFavoriteTitle(feedUrl: String?, newTitle: String?) {
		if (!feedUrl.isNullOrBlank() && !newTitle.isNullOrBlank()) {
			allFavorites = allFavorites.toMutableList().onEach {
				if (it.url() == feedUrl) it.title = newTitle
			}.toTypedArray()
		}
	}

	private fun Article?.safeBookmark() = this != null && !this.url.isNullOrBlank()

	var allBookmarks: Array<Article>
		get() = bookmarksStore.read(BOOKMARKS, Array<Article>::class.java) ?: arrayOf()
		set(value) {
			tryOrNull { bookmarksStore.write<Array<Article>>(BOOKMARKS, value.filter { it.safeBookmark() }.distinctBy { it.url }.toTypedArray()) }
		}

	private val allBookmarkUrls
		get() = allBookmarks.map { it.url }

	private fun addBookmarks(vararg articles: Article?) {
		allBookmarks += articles.filterNotNull().filter { !isSavedBookmark(it.url) }
	}

	fun addBookmark(article: Article?) {
		tryOrNull(execute = article.safeBookmark()) {
			addBookmarks(article)
		}
	}

	fun deleteBookmark(url: String?) {
		tryOrNull(execute = !url.isNullOrBlank()) {
			allBookmarks = allBookmarks.filter { it.url != url }.toTypedArray()
		}
	}

	var allReadUrls: Array<String>
		get() = readUrlsStore.read(READ_URLS, Array<String>::class.java) ?: arrayOf()
		set(value) {
			tryOrNull { readUrlsStore.write<Array<String>>(READ_URLS, value.distinct().toTypedArray()) }
		}

	fun addReadUrl(url: String?) = url?.let { allReadUrls += it }

	private fun Feed?.safeLastFeed() = this != null && !this.url().isNullOrBlank()

	var allLastFeeds: Array<Feed>
		get() = lastFeedsStore.read(LAST_FEEDS, Array<Feed>::class.java) ?: arrayOf()
		set(value) {
			tryOrNull { lastFeedsStore.write<Array<Feed>>(LAST_FEEDS, value.filter { it.safeLastFeed() }.toTypedArray()) }
		}

	fun addLastFeed(feed: Feed?) {
		if (feed != null) {
			allLastFeeds = allLastFeeds.filter { it.url() != feed.url() }.toTypedArray()
			allLastFeeds += feed
		}
	}

	fun isSavedFavorite(url: String?) = allFavoritesUrls.contains(url)

	fun isSavedBookmark(url: String?) = allBookmarkUrls.contains(url)

	fun isSavedReadUrl(url: String?) = allReadUrls.contains(url)

}
