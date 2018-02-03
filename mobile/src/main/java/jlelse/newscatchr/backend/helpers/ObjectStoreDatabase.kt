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
import java.util.*

/**
 * ObjectStoreDatabase
 */
class ObjectStoreDatabase : IDatabase {

	private val FAVORITES = "feeds_database"
	private val favoritesStore = KeyObjectStore(appContext, FAVORITES)
	private val BOOKMARKS = "bookmarks_database"
	private val bookmarksStore = KeyObjectStore(appContext, BOOKMARKS)
	private val READURLS = "urls_database"
	private val readUrlsStore = KeyObjectStore(appContext, READURLS)
	private val LASTFEEDS = "last_feeds"
	private val lastFeedsStore = KeyObjectStore(appContext, LASTFEEDS)

	override var allFavorites: MutableList<Feed>
		get() = favoritesStore.read(FAVORITES, Array<Feed>::class.java)?.toMutableList()
				?: mutableListOf()
		set(value) {
			tryOrNull { favoritesStore.write<Array<Feed>>(FAVORITES, value.filter { it.safeFavorite() }.distinctBy { it.url() }.toTypedArray()) }
		}
	override var allBookmarks: MutableList<Article>
		get() = bookmarksStore.read(BOOKMARKS, Array<Article>::class.java)?.toMutableList()
				?: mutableListOf()
		set(value) {
			tryOrNull { bookmarksStore.write<Array<Article>>(BOOKMARKS, value.filter { it.safeBookmark() }.distinctBy { it.url }.toTypedArray()) }
		}
	override var allReadUrls: MutableList<String>
		get() = readUrlsStore.read(READURLS, Array<String>::class.java)?.toMutableList()
				?: mutableListOf()
		set(value) {
			tryOrNull { readUrlsStore.write<Array<String>>(READURLS, value.distinct().toTypedArray()) }
		}
	override var allLastFeeds: MutableList<Feed>
		get() = lastFeedsStore.read(LASTFEEDS, Array<Feed>::class.java)?.toMutableList()
				?: mutableListOf()
		set(value) {
			tryOrNull { lastFeedsStore.write<Array<Feed>>(LASTFEEDS, value.filter { it.safeLastFeed() }.toTypedArray()) }
		}

	override fun addFavorites(vararg feeds: Feed?) {
		allFavorites = allFavorites.apply {
			addAll(feeds.filterNotNull().filter { !isFavorite(it.url()) })
		}
	}

	override fun deleteFavorite(url: String?) {
		if (!url.isNullOrBlank()) allFavorites = allFavorites.apply {
			removeAll { it.url() == url }
		}
	}

	override fun updateFavoriteTitle(feedUrl: String?, newTitle: String?) {
		if (!feedUrl.isNullOrBlank() && !newTitle.isNullOrBlank()) allFavorites = allFavorites.apply {
			forEach {
				if (it.url() == feedUrl) it.title = newTitle
			}
		}
	}

	override fun swapFavorites(position1: Int, position2: Int) {
		allFavorites = allFavorites.apply {
			Collections.swap(this, position1, position2)
		}
	}

	override fun isFavorite(url: String?) = !url.isNullOrBlank() && allFavorites.any { it.url() == url }

	override fun addBookmark(article: Article?) {
		if (article?.safeBookmark() == true && !isBookmark(article.url)) allBookmarks = allBookmarks.apply {
			add(article)
		}
	}

	override fun deleteBookmark(url: String?) {
		if (!url.isNullOrBlank()) allBookmarks = allBookmarks.apply {
			removeAll { it.url == url }
		}
	}

	override fun isBookmark(url: String?) = !url.isNullOrBlank() && allBookmarks.any { it.url == url }

	override fun addReadUrl(url: String?) {
		if (!url.isNullOrBlank()) url?.let {
			allReadUrls = allReadUrls.apply {
				add(it)
			}
		}
	}

	override fun isReadUrl(url: String?) = allReadUrls.contains(url)

	override fun addLastFeed(feed: Feed?) {
		if (feed.safeLastFeed() && feed != null) {
			allLastFeeds = allLastFeeds.apply {
				removeAll { it.url() == feed.url() }
				add(feed)
			}
		}
	}

	override fun deleteAllLastFeeds() {
		allLastFeeds = allLastFeeds.apply {
			clear()
		}
	}
}
