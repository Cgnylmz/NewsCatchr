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

package jlelse.feedly

import com.afollestad.ason.Ason
import com.afollestad.ason.AsonName
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import jlelse.commons.Deserializer
import jlelse.commons.ListDeserializer

object Feedly {

	private const val base = "https://cloud.feedly.com/v3"

	fun streamIds(id: String?, count: Int? = null, continuation: String? = null, ranked: String? = null): Ids? = try {
		"$base/streams/ids".httpGet(mutableListOf<Pair<String, Any?>>(
				"streamId" to id
		).apply {
			if (count != null) add("count" to count)
			if (!continuation.isNullOrBlank()) add("continuation" to continuation)
			if (!ranked.isNullOrBlank()) add("ranked" to ranked)
		}).responseObject(Deserializer(Ids::class.java)).third.component1()
	} catch (e: Exception) {
		null
	}

	fun mixIds(id: String?, count: Int? = null): Ids? = try {
		"$base/mixes/ids".httpGet(mutableListOf<Pair<String, Any?>>(
				"streamId" to id
		).apply {
			if (count != null) add("count" to count)
		}).responseObject(Deserializer(Ids::class.java)).third.component1()
	} catch (e: Exception) {
		null
	}

	fun entries(ids: List<String>): List<Article>? = try {
		if (ids.isNotEmpty()) "$base/entries/.mget".httpPost()
				.body(Ason.serializeList(ids).toString())
				.header("Content-Type" to "application/json")
				.responseObject(ListDeserializer(Article::class.java)).third.component1()
		else null
	} catch (e: Exception) {
		null
	}

	fun articleSearch(id: String?, query: String?): ArticleSearch? = try {
		"$base/search/contents".httpGet(listOf(
				"streamId" to id,
				"query" to query,
				"ct" to "feedly.desktop"
		)).responseObject(Deserializer(ArticleSearch::class.java)).third.component1()
	} catch (e: Exception) {
		null
	}

	data class Article(
			var id: String = "abc",
			var title: String? = null,
			@AsonName(name = "cannonical.$0.href")
			var canonical: String? = null,
			@AsonName(name = "content.content")
			var content: String? = null,
			@AsonName(name = "summary.content")
			var summary: String? = null,
			var author: String? = null,
			var published: Long = 0,
			@AsonName(name = "alternate.$0.href")
			var alternate: String? = null,
			@AsonName(name = "origin.title")
			var origin: String? = null,
			var keywords: List<String>? = null,
			@AsonName(name = "visual.url")
			var visual: String? = null,
			var cdnAmpUrl: String? = null
	)

	data class Ids(var ids: List<String>? = null, var continuation: String? = null)

	data class ArticleSearch(var id: String? = null, var title: String? = null, var items: List<Article>? = null)

}


