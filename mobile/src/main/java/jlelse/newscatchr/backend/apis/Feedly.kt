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

import com.afollestad.bridge.Bridge
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.readFromCache
import jlelse.newscatchr.backend.helpers.saveToCache
import jlelse.newscatchr.extensions.tryOrNull
import java.util.*

class Feedly {

	private val urlBase = "https://cloud.feedly.com/v3"
	private val urlStreamId = "streamId="
	private val urlContinuation = "continuation="
	private val urlCount = "count="
	private val urlRanked = "ranked="
	private val urlQuery = "query="

	fun feedSearch(query: String?, count: Int? = null, locale: String? = null, promoted: Boolean? = null, callback: (feeds: Array<Feed>?, related: Array<String>?) -> Unit) {
		var feeds: Array<Feed>? = null
		var related: Array<String>? = null
		tryOrNull {
			var url = "$urlBase/search/feeds?$urlQuery%s"
			if (count != null) url += "&$urlCount$count"
			if (!locale.isNullOrBlank()) url += "&locale=$locale"
			if (promoted != null) url += "&promoted=$promoted"
			val search = Bridge.get(url, query).asClass(FeedSearch::class.java)
			feeds = search?.results
			related = search?.related
		}
		callback(feeds, related)
	}

	fun recommendedFeeds(locale: String? = Locale.getDefault().language, cache: Boolean, callback: (feeds: Array<Feed>?, related: Array<String>?) -> Unit) {
		var feeds: Array<Feed>? = if (cache) readFromCache("recFeeds$locale", Array<Feed>::class.java) else null
		var related: Array<String>? = if (cache) readFromCache("recFeedsRelated$locale", Array<String>::class.java) else null
		if (!cache || feeds == null) {
			tryOrNull {
				feedSearch("news", 30, locale, true) { feedsTemp, relatedTemp ->
					feeds = feedsTemp?.take(30)?.toTypedArray().apply { saveToCache("recFeeds$locale") }
					related = relatedTemp?.apply { saveToCache("recFeedsRelated$locale") }
				}
			}
		}
		callback(feeds, related)
	}

	class Ids(var ids: Array<String>? = null, var continuation: String? = null)

	class FeedSearch(var results: Array<Feed>? = null, var related: Array<String>? = null)

}


