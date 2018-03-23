package jlelse.earl

import com.einmalfel.earl.EarlParser
import com.einmalfel.earl.Feed
import com.github.kittinunf.fuel.httpGet

class EarlGetter {

	fun getFeed(feed: String?, maxItems: Int = 20): Pair<Feed?, String?> {
		var feedUrl = feed
		if (feedUrl.isNullOrBlank()) return Pair(null, "Feed URL must not be null")
		if (feedUrl!!.startsWith("feed/")) feedUrl = feedUrl.substring(5)
		val (feedBytes, error) = feedUrl.httpGet().response().third
		return if (feedBytes != null && error == null) {
			try {
				val earlFeed = EarlParser.parse(feedBytes.inputStream(), maxItems)
				(Pair(earlFeed, null))
			} catch (e: Exception) {
				(Pair(null, e.message))
			}
		} else {
			(Pair(null, "Downloading feed failed."))
		}
	}

}