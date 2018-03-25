package jlelse.earl

import android.content.Context
import com.einmalfel.earl.Feed
import jlelse.sourcebase.Article
import jlelse.sourcebase.SourceLoader

class EarlLoader : SourceLoader() {

	override fun items(context: Context, cache: Boolean): List<Article>? = when (type) {
		SourceLoader.FeedTypes.FEED -> {
			parseArticles(EarlGetter().getFeed(feedUrl, count).first)
		}
		SourceLoader.FeedTypes.SEARCH -> {
			parseArticles(EarlGetter().getFeed(feedUrl, count).first)?.filter {
				if (query != null)
					it.title?.contains(query!!) ?: false || it.content?.contains(query!!) ?: false
				else true
			}
		}
		else -> null
	}?.let {
		when (ranked) {
			SourceLoader.Ranked.NEWEST -> it.sortedByDescending { it.time }
			SourceLoader.Ranked.OLDEST -> it.sortedBy { it.time }
		}
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