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

import android.support.annotation.Keep
import com.afollestad.ason.Ason
import com.afollestad.ason.AsonName
import com.afollestad.bridge.Bridge
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.extensions.tryOrNull
import org.jetbrains.anko.doAsync

/**
 * Everything has to be called from async thread
 */
class Pocket {

	private val pocketBase = "https://getpocket.com"

	fun add(url: String): String? = tryOrNull {
		Bridge.post("$pocketBase/v3/add")
				.header("Host", "getpocket.com")
				.contentType("application/json; charset=UTF-8")
				.header("X-Accept", "application/json")
				.body(Ason().put("url", url)
						.put("consumer_key", PocketApiKey)
						.put("access_token", Preferences.pocketAccessToken))
				.asAsonObject()?.getString("item_id")
	}

	fun archive(itemId: String) {
		tryOrNull {
			Bridge.post("$pocketBase/v3/send")
					.header("Host", "getpocket.com")
					.contentType("application/json; charset=UTF-8")
					.header("X-Accept", "application/json")
					.body(Ason().put("consumer_key", PocketApiKey)
							.put("access_token", Preferences.pocketAccessToken)
							.put("actions.$0.item_id", itemId)
							.put("actions.$0.action", "archive"))
					.request()
		}
	}

	fun get(): Array<GetResponseItem>? = tryOrNull {
		mutableListOf<GetResponseItem>().apply {
			Bridge.post("$pocketBase/v3/get")
					.header("Host", "getpocket.com")
					.contentType("application/json; charset=UTF-8")
					.header("X-Accept", "application/json")
					.body(Ason().put("consumer_key", PocketApiKey)
							.put("access_token", Preferences.pocketAccessToken)
							.put("detailType", "complete"))
					?.asJsonObject()
					?.optJSONObject("list")
					?.let {
						for (i in 0 until (it.names()?.length() ?: 1)) {
							(it.get(it.names().getString(i))?.toString() ?: "").let {
								add(Ason.deserialize(it, GetResponseItem::class.java))
							}
						}
					}
		}.toTypedArray()
	}
}

@Keep
class GetResponseItem(
		var item_id: String? = null,
		var given_url: String? = null,
		var resolved_title: String? = null,
		var excerpt: String? = null,
		@AsonName(name = "images.1.src")
		var image: String? = null
)

/**
 * Everything can be called from UI thread
 */
class PocketAuth(private val pocketRedirectUri: String, private val pocketCallback: PocketAuthCallback) {
	private var pocketCode: String? = null
	private var accessToken: String? = null
	private var userName: String? = null
	private val pocketBase = "https://getpocket.com"

	fun startAuth() {
		doAsync {
			try {
				Bridge.post("$pocketBase/v3/oauth/request")
						.header("Host", "getpocket.com")
						.contentType("application/json; charset=UTF-8")
						.header("X-Accept", "application/json")
						.body(Ason().put("consumer_key", PocketApiKey)
								.put("redirect_uri", pocketRedirectUri))
						.response()
						?.let {
							when (it.header("X-Error-Code")) {
								"138", "140", "152", "199" -> pocketCallback.failed()
								else -> authorize(it.asAsonObject()?.getString("code") ?: "")
							}
						}
			} catch (exception: Exception) {
				pocketCallback.failed()
			}
		}
	}

	private fun authorize(code: String) {
		pocketCode = code.apply {
			if (isNullOrBlank()) pocketCallback.failed()
		}
		pocketCallback.authorize("$pocketBase/auth/authorize?request_token=$code&redirect_uri=$pocketRedirectUri")
	}

	fun authenticate() {
		if (pocketCode.isNullOrBlank()) pocketCallback.failed()
		else {
			doAsync {
				try {
					Bridge.post("$pocketBase/v3/oauth/authorize")
							.header("Host", "getpocket.com")
							.contentType("application/json; charset=UTF-8")
							.header("X-Accept", "application/json")
							.body(Ason().put("consumer_key", PocketApiKey)
									.put("code", pocketCode))
							.response()
							?.let {
								when (it.header("X-Error-Code")) {
									"138", "152", "181", "182", "185", "158", "159", "199" -> pocketCallback.failed()
									else -> {
										it.asAsonObject()?.let {
											authenticated(it.getString("access_token") ?: "", it.getString("username") ?: "")
										}
									}
								}
							}
				} catch (exception: Exception) {
					pocketCallback.failed()
				}
			}
		}
	}

	private fun authenticated(accessToken: String, userName: String) {
		this.accessToken = accessToken
		this.userName = userName
		if (accessToken.isBlank() || userName.isBlank()) pocketCallback.failed()
		else pocketCallback.authenticated(accessToken, userName)
	}

	interface PocketAuthCallback {
		fun failed()
		fun authorize(url: String)
		fun authenticated(accessToken: String, userName: String)
	}

}