package jlelse.newscatchr.extensions

import android.app.Activity
import co.metalab.asyncawait.async
import jlelse.newscatchr.backend.apis.share
import jlelse.newscatchr.backend.apis.shortUrl
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.database
import jlelse.sourcebase.Article

fun Article.share(context: Activity) {
	async {
		val newUrl = await {
			if (Preferences.urlShortener) tryOrNull { link?.shortUrl() } ?: link else link
		}
		context.share("\"$title\"", "$title - $newUrl")
	}
}

fun Article?.isBookmark(): Boolean = database.isBookmark(this?.link)
fun Article.excerpt() = content?.toHtml().toString().buildExcerpt(30)