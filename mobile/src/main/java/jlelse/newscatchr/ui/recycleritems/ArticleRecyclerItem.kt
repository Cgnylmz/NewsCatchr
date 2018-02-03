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

package jlelse.newscatchr.ui.recycleritems

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.isBookmark
import jlelse.newscatchr.backend.share
import jlelse.newscatchr.database
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.fragments.ArticleView
import jlelse.newscatchr.ui.layout.ArticleRecyclerItemUI
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

class ArticleRecyclerItem(val article: Article? = null, val fragment: ViewManagerView? = null) : NCAbstractItem<ArticleRecyclerItem, ArticleRecyclerItem.ViewHolder>() {

	override fun getType(): Int {
		return R.id.article_item_id
	}

	override fun createView(ctx: Context, parent: ViewGroup?): View {
		return ArticleRecyclerItemUI().createView(AnkoContext.create(ctx, this))
	}

	override fun bindView(viewHolder: ViewHolder, payloads: MutableList<Any?>) {
		super.bindView(viewHolder, payloads)
		val context = viewHolder.itemView.context
		if (!article?.title.isNullOrBlank()) {
			viewHolder.title.showView()
			viewHolder.title.text = article?.title
			viewHolder.title.setTypeface(null, if (database.isReadUrl(article?.url)) Typeface.BOLD_ITALIC else Typeface.BOLD)
		} else viewHolder.title.hideView()
		if ((article?.published?.toInt() ?: 0) != 0) {
			viewHolder.details.showView()
			val detailText = DateUtils.getRelativeTimeSpanString(article!!.published)
			viewHolder.details.text = detailText
		} else {
			viewHolder.details.hideView()
		}
		if (!article?.content.isNullOrBlank()) {
			viewHolder.content.showView()
			viewHolder.content.text = article?.excerpt
		} else {
			viewHolder.content.hideView()
		}
		if (article?.keywords.notNullAndEmpty()) {
			viewHolder.tagsBox.showView()
			viewHolder.tagsBox.removeAllViews()
			viewHolder.tagsBox.addTags(fragment!!, article?.keywords?.take(3)?.toTypedArray())
		} else {
			//viewHolder.tagsBox.hideView()
		}
		if (!article?.visualUrl.isNullOrBlank()) {
			viewHolder.visual.showView()
			viewHolder.visual.loadImage(article?.visualUrl)
		} else {
			viewHolder.visual.clearGlide()
			viewHolder.visual.hideView()
		}
		viewHolder.itemView.setOnClickListener {
			if (article != null) fragment?.openView(ArticleView(article = article).withTitle(article.originTitle))
		}
		viewHolder.bookmark.setImageDrawable((if (article.isBookmark()) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, R.color.colorPrimaryText.resClr(context)))
		viewHolder.bookmark.setOnClickListener {
			if (article != null) {
				if (article.isBookmark()) {
					database.deleteBookmark(article.url)
					viewHolder.bookmark.setImageDrawable(R.drawable.ic_bookmark_border_universal.resDrw(context, R.color.colorPrimaryText.resClr(context)))
				} else {
					database.addBookmark(article)
					viewHolder.bookmark.setImageDrawable(R.drawable.ic_bookmark_universal.resDrw(context, R.color.colorPrimaryText.resClr(context)))
				}
			}
		}
		viewHolder.share.setImageDrawable(R.drawable.ic_share_universal.resDrw(context, R.color.colorPrimaryText.resClr(context)))
		viewHolder.share.setOnClickListener {
			if (fragment != null) article?.share(fragment.context)
		}
	}

	override fun getViewHolder(p0: View) = ViewHolder(p0)

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		var bookmark: ImageView = view.find(R.id.articlerecycleritem_bookmark)
		var share: ImageView = view.find(R.id.articlerecycleritem_share)
		var title: TextView = view.find(R.id.articlerecycleritem_title)
		var details: TextView = view.find(R.id.articlerecycleritem_details)
		var content: TextView = view.find(R.id.articlerecycleritem_content)
		var visual: ImageView = view.find(R.id.articlerecycleritem_visual)
		var tagsBox: FlexboxLayout = view.find(R.id.articlerecycleritem_tagsbox)
	}
}
