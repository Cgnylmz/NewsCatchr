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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import com.afollestad.bridge.Bridge
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.extensions.blankNull
import jlelse.newscatchr.extensions.resClr
import jlelse.newscatchr.extensions.resStr
import jlelse.readit.R
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import java.net.URLEncoder

// Share
fun Context.share(title: String, text: String) {
	startActivity(Intent.createChooser(Intent().apply {
		action = Intent.ACTION_SEND
		type = "text/plain"
		putExtra(Intent.EXTRA_SUBJECT, title)
		putExtra(Intent.EXTRA_TEXT, text)
	}, "${R.string.share.resStr()} $title"))
}

// Short Url
fun String.shortUrl(): String {
	if (isNotBlank()) {
		Bridge.get("https://tny.im/yourls-api.php?action=shorturl&format=simple&url=$this")
				.asString()
				.let { return it ?: this }
	} else return this
}

// Hastebin
fun String.uploadHaste(): String? {
	if (isNotBlank()) {
		Bridge.post("https://hastebin.com/documents")
				.body(this)
				.contentType("plain/text")
				.asAsonObject()
				.let {
					return it?.getString("key").blankNull()
				}
	} else return null
}

fun String.downloadHaste(): String? {
	if (isNotBlank()) {
		Bridge.get("https://hastebin.com/documents/$this")
				.asAsonObject()
				.let {
					return it?.getString("data").blankNull()
				}
	} else return null
}

// Readability
fun String.fetchArticle(oldArticle: Article? = null): Article? {
	Bridge.get("https://mercury.postlight.com/parser?url=$this")
			.contentType("application/json")
			.header("x-api-key", ReadabilityApiKey)
			.asAsonObject()
			?.let {
				return (oldArticle ?: Article()).apply {
					url = it.getString("url").blankNull() ?: oldArticle?.url ?: this@fetchArticle
					canonicalHref = null
					alternateHref = null
					title = it.getString("title").blankNull() ?: oldArticle?.title
					content = it.getString("content").blankNull() ?: oldArticle?.title
					summaryContent = null
					visualUrl = it.getString("lead_image_url").blankNull() ?: oldArticle?.visualUrl
					enclosureHref = null
					process(force = true)
				}
			}
	return null
}

// AMP
fun String.ampUrl() = "https://mercury.postlight.com/amp?url=${URLEncoder.encode(this, "UTF-8")}"

// Open Url
fun String?.openUrl(activity: Activity, amp: Boolean = true, isAmp: Boolean = false, notAmpLink: String? = null) {
	val ampAllowed = Preferences.amp && amp
	(if (ampAllowed && (!isAmp || (isAmp && this@openUrl.isNullOrBlank()))) (this@openUrl ?: notAmpLink)?.ampUrl()
	else if (ampAllowed && isAmp) this@openUrl
	else notAmpLink ?: this@openUrl)?.let { finalUrl ->
		val alternateIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
		if (Preferences.customTabs) {
			try {
				val customTabsIntent = CustomTabsIntent.Builder()
						.setToolbarColor(R.color.colorPrimary.resClr(activity)!!)
						.setShowTitle(true)
						.addDefaultShareMenuItem()
						.enableUrlBarHiding()
						.build()
				CustomTabsHelperFragment.open(activity, customTabsIntent, Uri.parse(finalUrl)) { activity, _ ->
					activity.startActivity(alternateIntent)
				}
			} catch (e: Exception) {
				e.printStackTrace()
				activity.startActivity(alternateIntent)
			}
		} else {
			activity.startActivity(alternateIntent)
		}
	}
}