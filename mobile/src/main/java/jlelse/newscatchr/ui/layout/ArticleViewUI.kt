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

package jlelse.newscatchr.ui.layout

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.View
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import jlelse.newscatchr.extensions.flexboxLayout
import jlelse.newscatchr.extensions.imageView
import jlelse.newscatchr.extensions.setTextStyle
import jlelse.newscatchr.extensions.swipeRefreshLayout
import jlelse.newscatchr.extensions.textView
import jlelse.newscatchr.extensions.zoomTextView
import jlelse.newscatchr.ui.fragments.ArticleView
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView

class ArticleViewUI : AnkoComponent<ArticleView> {
	@SuppressLint("PrivateResource")
	override fun createView(ui: AnkoContext<ArticleView>): View = with(ui) {
		swipeRefreshLayout {
			id = R.id.articlefragment_refresh
			nestedScrollView {
				id = R.id.articlefragment_scrollview
				verticalLayout {
					lparams(width = matchParent, height = wrapContent)
					textView {
						lparams(width = matchParent, height = wrapContent) {
							topMargin = dip(32)
							bottomMargin = dip(8)
							horizontalMargin = dip(16)
						}
						id = R.id.articlefragment_title
						setTextStyle(context, R.style.TextAppearance_AppCompat_Headline)
						setTypeface(typeface, Typeface.BOLD)
					}
					imageView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = dip(8)
							horizontalMargin = dip(16)
						}
						id = R.id.articlefragment_visual
						adjustViewBounds = true
					}
					textView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = dip(8)
							horizontalMargin = dip(16)
						}
						id = R.id.articlefragment_details
						setTextStyle(context, R.style.TextAppearance_AppCompat_Caption)
					}
					zoomTextView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = dip(8)
							horizontalMargin = dip(16)
						}
						id = R.id.articlefragment_content
						setTextStyle(context, R.style.TextAppearance_AppCompat_Body1)
						setTextIsSelectable(true)
						textSize = 16f
					}
					textView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = dip(8)
							horizontalMargin = dip(16)
						}
						textResource = R.string.article_tip_zoom
						setTextStyle(context, R.style.TextAppearance_AppCompat_Caption)
						setTypeface(typeface, Typeface.ITALIC)
					}
					flexboxLayout {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = dip(16)
							horizontalPadding = dip(12)
							visibility = View.GONE
						}
						id = R.id.articlefragment_tagsbox
						flexWrap = FlexWrap.WRAP
						justifyContent = JustifyContent.FLEX_START
					}
				}
			}
		}
	}
}
