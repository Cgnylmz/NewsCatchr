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

package jlelse.newscatchr.backend.loaders

import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.backend.helpers.getCachedArticle
import jlelse.newscatchr.backend.helpers.isArticleCached
import jlelse.newscatchr.backend.helpers.readFromCache
import jlelse.newscatchr.backend.helpers.saveToCache

class FeedlyLoader {
	var type: FeedTypes? = null
	var count = 20
	var query: String? = null
	var feedUrl: String? = null
	var ranked: Ranked = Ranked.NEWEST
	var continuation: String? = null

	fun items(cache: Boolean): List<Article>? = when (type) {
		FeedTypes.MIX -> {
			var ids: Feedly.Ids? = if (cache) readFromCache("MixIds$feedUrl" + when (ranked) {
				Ranked.OLDEST -> "oldest"
				else -> ""
			}, Feedly.Ids::class.java) else null
			if (ids == null) {
				ids = Feedly().mixIds(feedUrl, count).apply {
					saveToCache("MixIds$feedUrl" + when (ranked) {
						Ranked.OLDEST -> "oldest"
						else -> ""
					})
				}
			}
			itemsByIds(ids?.ids, cache)
		}
		FeedTypes.FEED -> {
			var ids: Feedly.Ids? = if (cache) readFromCache("StreamIds$feedUrl" + when (ranked) {
				Ranked.OLDEST -> "oldest"
				else -> ""
			}, Feedly.Ids::class.java) else null
			if (ids == null) {
				ids = Feedly().streamIds(feedUrl, count, null, when (ranked) {
					Ranked.NEWEST -> "newest"
					Ranked.OLDEST -> "oldest"
				}).apply {
					saveToCache("StreamIds$feedUrl" + when (ranked) {
						Ranked.OLDEST -> "oldest"
						else -> ""
					})
				}
			}
			continuation = ids?.continuation
			itemsByIds(ids?.ids, cache)
		}
		FeedTypes.SEARCH -> Feedly().articleSearch(feedUrl, query)?.items
		else -> null
	}?.onEach { it.process().saveToCache() }

	fun moreItems(): List<Article>? = itemsByIds(
			Feedly().streamIds(feedUrl, count, continuation, when (ranked) {
				Ranked.NEWEST -> "newest"
				Ranked.OLDEST -> "oldest"
			})?.apply {
				this@FeedlyLoader.continuation = continuation
			}?.ids, true
	)?.onEach { it.process().saveToCache() }

	private fun itemsByIds(ids: Array<String>?, cache: Boolean): List<Article>? = if (ids != null && ids.isNotEmpty()) {
		ids.filter { if (cache) !isArticleCached(it) else true }.let {
			Feedly().entries(it)?.forEach { it.saveToCache() }
		}
		ids.mapNotNull { getCachedArticle(it) }
	} else null

	enum class FeedTypes { FEED, SEARCH, MIX }
	enum class Ranked { NEWEST, OLDEST }

}
