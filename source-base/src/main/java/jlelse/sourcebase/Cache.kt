package jlelse.sourcebase

import android.content.Context
import jlelse.kos.KeyObjectStore

fun <T> T?.saveToCache(context: Context, key: String?) = KeyObjectStore(context, name = "sourceCache", cache = true).write<T>(key?.formatForCache(), this)

fun <T> readFromCache(context: Context, key: String?, type: Class<T>): T? = KeyObjectStore(context, name = "sourceCache", cache = true).read(key?.formatForCache(), type)

fun String.formatForCache(): String = try {
	replace("[^0-9a-zA-Z]".toRegex(), "")
} catch (e: Exception) {
	this
}

fun isArticleCached(context: Context, id: String): Boolean = KeyObjectStore(context, name = "articleCache", cache = true).exists(id.formatForCache())

fun getCachedArticle(context: Context, id: String): Article? = try {
	if (isArticleCached(context, id)) KeyObjectStore(context, name = "articleCache", cache = true).read(id.formatForCache(), Article::class.java)
	else null
} catch (e: Exception) {
	null
}

fun Article.saveToCache(context: Context) {
	if (!id.isBlank()) jlelse.kos.KeyObjectStore(context, name = "articleCache", cache = true).write<Article>(id.formatForCache(), this)
}