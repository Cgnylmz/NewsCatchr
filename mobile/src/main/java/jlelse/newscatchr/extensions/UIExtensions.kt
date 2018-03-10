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

package jlelse.newscatchr.extensions

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import jlelse.newscatchr.backend.apis.openUrl
import jlelse.newscatchr.mainAcivity
import jlelse.readit.R

fun ImageView.loadImage(url: String?) {
	clearGlide()
	tryOrNull { Glide.with(this).load(url).into(this) }
}

fun ImageView.clearGlide() {
	tryOrNull { Glide.with(this).clear(this) }
}

fun Context.nothingFound(callback: () -> Unit = {}) {
	MaterialDialog.Builder(this)
			.content(R.string.nothing_found)
			.positiveText(android.R.string.ok)
			.onAny { _, _ -> callback() }
			.cancelListener { callback() }
			.show()
}

fun TextView.setTextStyle(context: Context, id: Int) {
	@Suppress("DEPRECATION")
	if (Build.VERSION.SDK_INT < 23) setTextAppearance(context, id)
	else setTextAppearance(id)
}

fun TextView.applyLinks(amp: Boolean = true) {
	movementMethod = LinkMovementMethod.getInstance()
	text.let { tempText ->
		if (tempText is Spannable) {
			text = SpannableStringBuilder(tempText).apply {
				clearSpans()
				tempText.getSpans(0, text.length, URLSpan::class.java).forEach {
					setSpan(object : ClickableSpan() {
						override fun onClick(view: View?) {
							mainAcivity?.let { activity -> it.url.openUrl(activity, amp) }
						}
					}, tempText.getSpanStart(it), tempText.getSpanEnd(it), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
				}
			}
		}
	}
}

fun Context.progressDialog(): MaterialDialog = MaterialDialog.Builder(this).content(R.string.loading).cancelable(false).progress(true, 0).build()