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

package jlelse.newscatchr.backend.apis

import com.github.kittinunf.fuel.httpGet
import jlelse.commons.Deserializer
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.readFromCache
import jlelse.newscatchr.backend.helpers.saveToCache
import jlelse.newscatchr.extensions.tryOrNull
import java.util.*

class Feedly {

	private val urlBase = "https://cloud.feedly.com/v3"

	fun feedSearch(query: String?, count: Int? = null, locale: String? = null, promoted: Boolean? = null, callback: (feeds: List<Feed>?, related: List<String>?) -> Unit) {
		try {
			val search = "$urlBase/search/feeds".httpGet(mutableListOf<Pair<String, Any?>>(
					"query" to query
			).apply {
				if (count != null) add("count" to count)
				if (!locale.isNullOrBlank()) add("locale" to locale)
				if (promoted != null) add("promoted" to promoted)
			}).responseObject(Deserializer(FeedSearch::class.java)).third.component1()
			callback(search?.results, search?.related)
		} catch (e: Exception) {
			callback(null, null)
		}
	}

	fun recommendedFeeds(locale: String? = Locale.getDefault().language, cache: Boolean, callback: (feeds: List<Feed>?, related: List<String>?) -> Unit) {
		var feeds: List<Feed>? = if (cache) readFromCache("recFeeds$locale", listOf<Feed>().javaClass) else null
		var related: List<String>? = if (cache) readFromCache("recFeedsRelated$locale", listOf<String>().javaClass) else null
		if (!cache || feeds == null || related == null) {
			tryOrNull {
				feedSearch("news", 30, locale, true) { feedsTemp, relatedTemp ->
					feeds = feedsTemp?.take(30)?.apply { saveToCache("recFeeds$locale") }
					related = relatedTemp?.apply { saveToCache("recFeedsRelated$locale") }
				}
			}
		}
		callback(feeds, related)
	}

	class FeedSearch(var results: List<Feed>? = null, var related: List<String>? = null)

}


