package jlelse.feedly

import android.content.Context
import jlelse.sourcebase.Article
import jlelse.sourcebase.SourceLoader

class FeedlyLoader : SourceLoader() {

	override fun items(context: Context, cache: Boolean): List<Article>? = when (type) {
		SourceLoader.FeedTypes.MIX -> {
			val ids: Feedly.Ids? = Feedly.mixIds(feedUrl, count)
			itemsByIds(ids?.ids, cache)
		}
		SourceLoader.FeedTypes.FEED -> {
			val ids: Feedly.Ids? = Feedly.streamIds(feedUrl, count, null, when (ranked) {
				SourceLoader.Ranked.NEWEST -> "newest"
				SourceLoader.Ranked.OLDEST -> "oldest"
			})
			continuation = ids?.continuation
			itemsByIds(ids?.ids, cache)
		}
		SourceLoader.FeedTypes.SEARCH -> Feedly.articleSearch(feedUrl, query)?.items?.map {
			parseArticle(it)
		}
		else -> null
	}

	override fun moreItems(context: Context): List<Article>? = itemsByIds(
			Feedly.streamIds(feedUrl, count, continuation, when (ranked) {
				SourceLoader.Ranked.NEWEST -> "newest"
				SourceLoader.Ranked.OLDEST -> "oldest"
			})?.apply {
				this@FeedlyLoader.continuation = continuation
			}?.ids, true
	)

	private fun itemsByIds(ids: List<String>?, cache: Boolean): List<Article>? = if (ids != null && ids.isNotEmpty()) {
		Feedly.entries(ids)?.map { parseArticle(it) }
	} else null

	private fun parseArticle(article: Feedly.Article): Article {
		return Article(
				id = article.id,
				time = article.published,
				author = article.author,
				title = article.title,
				link = article.canonical ?: article.alternate,
				image = article.visual,
				feedTitle = article.origin,
				content = article.content ?: article.summary,
				amp = article.cdnAmpUrl,
				tags = article.keywords
		)
	}
}