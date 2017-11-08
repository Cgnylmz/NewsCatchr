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
import android.view.View
import jlelse.newscatchr.extensions.resClr
import jlelse.newscatchr.extensions.textView
import jlelse.newscatchr.ui.recycleritems.HeaderRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.*

class HeaderRecyclerItemUI : AnkoComponent<HeaderRecyclerItem> {
	override fun createView(ui: AnkoContext<HeaderRecyclerItem>): View = with(ui) {
		verticalLayout {
			textView {
				lparams(width = wrapContent, height = wrapContent) {
					margin = dip(16)
				}
				id = R.id.headerrecycleritem_textview
				textColor = R.color.colorAccent.resClr(context)!!
				textSize = 16f
				setTypeface(typeface, Typeface.BOLD)
			}
		}
	}
}