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

package jlelse.newscatchr.backend.loaders

import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.apis.Pocket

class PocketLoader {
	fun items(): List<Article>? = Pocket().get()?.map {
		Article(
				url = it.given_url,
				title = it.resolved_title,
				content = it.excerpt,
				pocketId = it.item_id,
				fromPocket = true
		).process()
	}
}
