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

import android.view.View
import android.view.ViewManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.newscatchr.ui.views.ZoomTextView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.flexboxLayout(theme: Int = 0, init: FlexboxLayout.() -> Unit) = ankoView(::FlexboxLayout, theme, init)
inline fun ViewManager.swipeRefreshLayout(theme: Int = 0, init: SwipeRefreshLayout.() -> Unit) = ankoView(::SwipeRefreshLayout, theme, init)
inline fun ViewManager.zoomTextView(theme: Int = 0, init: ZoomTextView.() -> Unit) = ankoView(::ZoomTextView, theme, init)
inline fun ViewManager.textView(theme: Int = 0, init: TextView.() -> Unit) = ankoView(::TextView, theme, init)
inline fun ViewManager.imageView(theme: Int = 0, init: ImageView.() -> Unit) = ankoView(::ImageView, theme, init)
inline fun ViewManager.view(theme: Int = 0, init: View.() -> Unit) = ankoView(::View, theme, init)
inline fun ViewManager.button(theme: Int = 0, init: Button.() -> Unit) = ankoView(::Button, theme, init)