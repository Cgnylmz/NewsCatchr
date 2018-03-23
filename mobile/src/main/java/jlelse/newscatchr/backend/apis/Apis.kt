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
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
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
	return if (isNotBlank()) {
		"https://tny.im/yourls-api.php".httpGet(listOf(
				"action" to "shorturl",
				"format" to "simple",
				"url" to this
		)).responseString().third.component1() ?: this
	} else this
}

// Hastebin
fun String.uploadHaste(): String? {
	return if (isNotBlank()) {
		"https://hastebin.com/documents".httpPost()
				.body(this)
				.header("Content-Type" to "plain/text")
				.responseJson().third.component1()?.obj()?.optString("key").blankNull()
	} else null
}

fun String.downloadHaste(): String? {
	return if (isNotBlank()) {
		"https://hastebin.com/documents/$this".httpGet()
				.responseJson().third.component1()?.obj()?.optString("data").blankNull()
	} else null
}

// AMP
fun String.ampUrl() = "https://mercury.postlight.com/amp?url=${URLEncoder.encode(this, "UTF-8")}"

// Open Url
fun String?.openUrl(activity: Activity, amp: Boolean = true, isAmp: Boolean = false, notAmpLink: String? = null) {
	val ampAllowed = Preferences.amp && amp
	(if (ampAllowed && (!isAmp || (isAmp && this@openUrl.isNullOrBlank()))) (this@openUrl
			?: notAmpLink)?.ampUrl()
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