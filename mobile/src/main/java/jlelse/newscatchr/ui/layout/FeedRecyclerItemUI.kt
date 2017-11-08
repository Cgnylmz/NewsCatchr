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

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import jlelse.newscatchr.extensions.imageView
import jlelse.newscatchr.extensions.resClr
import jlelse.newscatchr.extensions.setTextStyle
import jlelse.newscatchr.extensions.textView
import jlelse.newscatchr.extensions.view
import jlelse.newscatchr.ui.recycleritems.FeedRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.*

class FeedRecyclerItemUI : AnkoComponent<FeedRecyclerItem> {
	override fun createView(ui: AnkoContext<FeedRecyclerItem>): View = with(ui) {
		verticalLayout {
			lparams(width = matchParent, height = wrapContent) {
				topPadding = dip(16)
				horizontalPadding = dip(16)
			}
			context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground)).apply {
				backgroundResource = getResourceId(0, 0)
			}.recycle()
			verticalLayout {
				lparams(width = matchParent, height = wrapContent) {
					bottomPadding = dip(16)
				}
				orientation = LinearLayout.HORIZONTAL
				gravity = Gravity.TOP
				verticalLayout {
					lparams(width = 0, height = wrapContent, weight = 1f) {
						marginEnd = dip(16)
					}
					textView {
						lparams(width = matchParent, height = wrapContent)
						id = R.id.feedrecycleritem_title
						setTextStyle(context, R.style.TextAppearance_AppCompat_Medium)
						R.color.colorPrimaryText.resClr(context)?.let { textColor = it }
						typeface = Typeface.DEFAULT_BOLD
					}
					textView {
						lparams(width = matchParent, height = wrapContent)
						id = R.id.feedrecycleritem_website
						setTextStyle(context, R.style.TextAppearance_AppCompat_Small)
						textColor = R.color.colorSecondaryText.resClr(context)!!
					}
				}
				imageView {
					lparams(width = dip(36), height = dip(36)) {
						padding = dip(6)
					}
					id = R.id.feedrecycleritem_favorite
					context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackgroundBorderless)).apply {
						backgroundResource = getResourceId(0, 0)
					}.recycle()
				}
			}
			view {
				lparams(width = matchParent, height = dip(1))
				id = R.id.feedrecycleritem_divider
				backgroundColor = R.color.colorDivider.resClr(context)!!
			}
		}
	}
}