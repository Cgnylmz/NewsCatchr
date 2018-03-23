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

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.backend.helpers

import android.content.Context
import co.metalab.asyncawait.async
import com.bumptech.glide.Glide
import jlelse.kos.KeyObjectStore
import jlelse.newscatchr.appContext
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.sourcebase.Article

fun <T> T?.saveToCache(key: String?) = KeyObjectStore(appContext, name = "cache", cache = true).write<T>(key?.formatForCache(), this)

fun <T> readFromCache(key: String?, type: Class<T>): T? = KeyObjectStore(appContext, name = "cache", cache = true).read(key?.formatForCache(), type)

fun String.formatForCache(): String = tryOrNull { replace("[^0-9a-zA-Z]".toRegex(), "") } ?: this

fun isArticleCached(id: String): Boolean = KeyObjectStore(appContext, name = "article_cache", cache = true).exists(id.formatForCache())

fun getCachedArticle(id: String): Article? = tryOrNull {
	if (isArticleCached(id)) KeyObjectStore(appContext, name = "article_cache", cache = true).read(id.formatForCache(), Article::class.java)
	else null
}

fun Article.saveToCache() {
	if (!id.isBlank()) KeyObjectStore(appContext, name = "article_cache", cache = true).write<Article>(id.formatForCache(), this)
}

fun Context.clearCache(finished: () -> Unit?) {
	async {
		await {
			KeyObjectStore(appContext, name = "cache", cache = true).destroy()
			KeyObjectStore(appContext, name = "article_cache", cache = true).destroy()
			Glide.get(this@clearCache).clearDiskCache()
		}
		finished()
	}
}