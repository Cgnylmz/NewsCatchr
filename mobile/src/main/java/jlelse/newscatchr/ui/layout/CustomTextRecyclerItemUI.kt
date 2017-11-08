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
import jlelse.newscatchr.extensions.resClr
import jlelse.newscatchr.extensions.setTextStyle
import jlelse.newscatchr.extensions.textView
import jlelse.newscatchr.ui.recycleritems.CustomTextRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.*

class CustomTextRecyclerItemUI : AnkoComponent<CustomTextRecyclerItem> {
	override fun createView(ui: AnkoContext<CustomTextRecyclerItem>): View = with(ui) {
		verticalLayout {
			lparams(width = matchParent, height = wrapContent) {
				padding = dip(16)
			}
			textView {
				lparams(width = matchParent, height = wrapContent)
				id = R.id.customtextrecycleritem_text
				gravity = Gravity.CENTER
				setTextStyle(context, R.style.TextAppearance_AppCompat_Headline)
				setTypeface(typeface, Typeface.BOLD)
				R.color.colorSecondaryText.resClr(context)?.let { textColor = it }
			}
		}
	}
}