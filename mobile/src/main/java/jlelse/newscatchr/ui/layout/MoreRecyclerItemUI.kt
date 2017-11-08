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

import android.graphics.Color
import android.view.View
import jlelse.newscatchr.extensions.button
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.ui.recycleritems.MoreRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.*

class MoreRecyclerItemUI : AnkoComponent<MoreRecyclerItem> {
	override fun createView(ui: AnkoContext<MoreRecyclerItem>): View = with(ui) {
		verticalLayout {
			button {
				lparams(width = wrapContent, height = dip(48)) {
					horizontalMargin = dip(16)
					verticalMargin = dip(8)
				}
				id = R.id.morerecycleritem_button
				maxLines = 1
				text = R.string.more.resStr()
				textColor = Color.WHITE
			}
		}
	}
}