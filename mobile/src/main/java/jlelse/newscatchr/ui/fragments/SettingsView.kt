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

package jlelse.newscatchr.ui.fragments

import android.annotation.SuppressLint
import android.view.View
import jlelse.newscatchr.ui.layout.SettingsViewUI
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext

@SuppressLint("ViewConstructor")
class SettingsView : ViewManagerView() {
	override fun onCreateView(): View? {
		super.onCreateView()
		return SettingsViewUI().createView(AnkoContext.create(context, this))
	}
}
