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

package jlelse.newscatchr.ui.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.apis.openUrl
import jlelse.newscatchr.backend.helpers.ObjectStoreDatabase
import jlelse.newscatchr.backend.share
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.layout.ArticleViewUI
import jlelse.newscatchr.ui.recycleritems.addTags
import jlelse.newscatchr.ui.views.ZoomTextView
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

@SuppressLint("ViewConstructor")
class ArticleView(var article: Article) : ViewManagerView(), FAB {
	private var fragmentView: View? = null
	private val titleView: TextView? by lazy { fragmentView?.find<TextView>(R.id.articlefragment_title) }
	private val visualView: ImageView? by lazy { fragmentView?.find<ImageView>(R.id.articlefragment_visual) }
	private val detailsView: TextView? by lazy { fragmentView?.find<TextView>(R.id.articlefragment_details) }
	private val tagsBox: FlexboxLayout? by lazy { fragmentView?.find<FlexboxLayout>(R.id.articlefragment_tagsbox) }
	private val articleContentView: ZoomTextView? by lazy { fragmentView?.find<ZoomTextView>(R.id.articlefragment_content) }

	private val bookmark
		get() = ObjectStoreDatabase.isBookmark(article.url)

	override val fabDrawable = R.drawable.ic_share
	override val fabClick = { shareArticle() }

	override fun onCreateView(): View? {
		super.onCreateView()
		fragmentView = ArticleViewUI().createView(AnkoContext.create(context, this))
		showArticle(article)
		ObjectStoreDatabase.addReadUrl(article.url)
		return fragmentView
	}

	private fun showArticle(article: Article?) {
		if (article != null) {
			this@ArticleView.article = article
			image(article.visualUrl)
			title(article.title)
			details(article.author, article.originTitle, article.published)
			content(article.content)
			keywords(article.keywords)
		}
	}

	private fun image(visualUrl: String? = "") {
		if (!visualUrl.isNullOrBlank()) visualView?.apply {
			showView()
			loadImage(visualUrl)
		} else visualView?.hideView()
	}

	private fun title(title: String? = "") {
		if (!title.isNullOrBlank()) titleView?.apply {
			showView()
			text = title?.toHtml()
		} else titleView?.hideView()
	}

	private fun details(author: String? = "", originTitle: String? = "", published: Long? = 0) {
		var details: String? = ""
		if (!author.isNullOrBlank()) details += author
		if (!originTitle.isNullOrBlank()) {
			if (!details.isNullOrBlank()) details += " - "
			details += originTitle
		}
		if ((published?.toInt() ?: 0) != 0) {
			if (!details.isNullOrBlank()) details += "\n"
			details += DateUtils.getRelativeTimeSpanString(published!!)
		}
		if (!details.isNullOrBlank()) detailsView?.apply {
			showView()
			text = details
		} else detailsView?.hideView()
	}

	private fun content(content: String? = "") {
		if (!content.isNullOrBlank()) articleContentView?.apply {
			showView()
			text = content?.toHtml()
			applyLinks(true)
		} else articleContentView?.hideView()
	}

	private fun keywords(keywords: Array<String>? = null) {
		if (keywords.notNullAndEmpty()) tagsBox?.apply {
			showView()
			removeAllViews()
			addTags(this@ArticleView, keywords)
		} else tagsBox?.hideView()
	}

	private fun shareArticle() = article.share(context)

	override fun inflateMenu(inflater: MenuInflater, menu: Menu?) {
		super.inflateMenu(inflater, menu)
		inflater.inflate(R.menu.articlefragment, menu)
		menu?.findItem(R.id.bookmark)?.icon = (if (bookmark) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, Color.WHITE)
	}

	override fun onOptionsItemSelected(item: MenuItem?) {
		super.onOptionsItemSelected(item)
		when (item?.itemId) {
			R.id.bookmark -> {
				if (!bookmark) ObjectStoreDatabase.addBookmark(article)
				else ObjectStoreDatabase.deleteBookmark(article.url)
				item.icon = (if (bookmark) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, Color.WHITE)
			}
			R.id.browser -> (article.cdnAmpUrl
					?: article.ampUrl).openUrl(context, isAmp = true, notAmpLink = article.url)
		}
	}
}
