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
import jlelse.newscatchr.backend.process

class FeedlyLoader : ILoader {
	override var type: ILoader.FeedTypes? = null
	override var count = 20
	override var query: String? = null
	override var feedUrl: String? = null
	override var ranked: ILoader.Ranked = ILoader.Ranked.NEWEST
	override var continuation: String? = null

	override fun items(cache: Boolean): List<Article>? = when (type) {
		ILoader.FeedTypes.MIX -> {
			var ids: Feedly.Ids? = if (cache) readFromCache("MixIds$feedUrl" + when (ranked) {
				ILoader.Ranked.OLDEST -> "oldest"
				else -> ""
			}, Feedly.Ids::class.java) else null
			if (ids == null) {
				ids = Feedly().mixIds(feedUrl, count).apply {
					saveToCache("MixIds$feedUrl" + when (ranked) {
						ILoader.Ranked.OLDEST -> "oldest"
						else -> ""
					})
				}
			}
			itemsByIds(ids?.ids, cache)
		}
		ILoader.FeedTypes.FEED -> {
			var ids: Feedly.Ids? = if (cache) readFromCache("StreamIds$feedUrl" + when (ranked) {
				ILoader.Ranked.OLDEST -> "oldest"
				else -> ""
			}, Feedly.Ids::class.java) else null
			if (ids == null) {
				ids = Feedly().streamIds(feedUrl, count, null, when (ranked) {
					ILoader.Ranked.NEWEST -> "newest"
					ILoader.Ranked.OLDEST -> "oldest"
				}).apply {
					saveToCache("StreamIds$feedUrl" + when (ranked) {
						ILoader.Ranked.OLDEST -> "oldest"
						else -> ""
					})
				}
			}
			continuation = ids?.continuation
			itemsByIds(ids?.ids, cache)
		}
		ILoader.FeedTypes.SEARCH -> Feedly().articleSearch(feedUrl, query)?.items
		else -> null
	}?.onEach { it.process().saveToCache() }

	override fun moreItems(): List<Article>? = itemsByIds(
			Feedly().streamIds(feedUrl, count, continuation, when (ranked) {
				ILoader.Ranked.NEWEST -> "newest"
				ILoader.Ranked.OLDEST -> "oldest"
			})?.apply {
				this@FeedlyLoader.continuation = continuation
			}?.ids, true
	)?.onEach { it.process().saveToCache() }

	override fun itemsByIds(ids: Array<String>?, cache: Boolean): List<Article>? = if (ids != null && ids.isNotEmpty()) {
		ids.filter { if (cache) !isArticleCached(it) else true }.let {
			Feedly().entries(it)?.forEach { it.saveToCache() }
		}
		ids.mapNotNull { getCachedArticle(it) }
	} else null

}
