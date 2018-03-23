package jlelse.earl

import android.content.Context
import com.einmalfel.earl.Feed
import jlelse.sourcebase.Article
import jlelse.sourcebase.SourceLoader

class EarlLoader : SourceLoader() {

	override fun items(context: Context, cache: Boolean): List<Article>? = when (type) {
		SourceLoader.FeedTypes.FEED -> {
			val feed = EarlGetter().getFeed(feedUrl, count).first
			val articles = parseArticles(feed)
			when (ranked) {
				SourceLoader.Ranked.NEWEST -> articles?.sortedByDescending { it.time }
				SourceLoader.Ranked.OLDEST -> articles?.sortedBy { it.time }
			}
		}
		else -> null
	}

	override fun moreItems(context: Context): List<Article>? = null

	private fun parseArticles(feed: Feed?): List<Article>? {
		return feed?.items?.map {
			Article(
					id = it.id ?: it.link ?: "",
					time = it.publicationDate?.time ?: 0,
					author = it.author,
					title = it.title,
					content = it.description,
					image = it.imageLink,
					link = it.link,
					feedTitle = feed.title
			)
		}
	}
}