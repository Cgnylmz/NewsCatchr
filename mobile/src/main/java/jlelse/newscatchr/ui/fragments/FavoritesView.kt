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
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import com.mikepenz.fastadapter_extensions.utilities.DragDropUtil
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.ui.layout.RefreshRecyclerUI
import jlelse.newscatchr.ui.recycleritems.CustomTextRecyclerItem
import jlelse.newscatchr.ui.recycleritems.FeedRecyclerItem
import jlelse.newscatchr.ui.recycleritems.NCAbstractItem
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import java.util.*

@SuppressLint("ViewConstructor")
class FavoritesView : ViewManagerView(), ItemTouchCallback {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.refreshrecyclerview_recycler) }
	private val feedAdapter = ItemAdapter<FeedRecyclerItem>()
	private val errorAdapter = ItemAdapter<CustomTextRecyclerItem>()
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.refreshrecyclerview_refresh) }
	private var feeds: MutableList<Feed>? = null

	override fun onCreateView(): View? {
		super.onCreateView()
		fragmentView = RefreshRecyclerUI().createView(AnkoContext.create(context, this))
		ItemTouchHelper(SimpleDragCallback(this)).attachToRecyclerView(recyclerOne)
		if (recyclerOne?.adapter == null) {
			val adapter: FastAdapter<NCAbstractItem<*, *>> = FastAdapter.with(listOf(feedAdapter, errorAdapter))
			recyclerOne?.adapter = adapter
		}
		refreshOne?.setOnRefreshListener { load() }
		load()
		return fragmentView
	}

	private fun load() {
		feeds = Database.allFavorites.toMutableList()
		if (feeds?.notNullAndEmpty() == true) feedAdapter.setNewList(feeds!!.map { FeedRecyclerItem(it, fragment = this@FavoritesView) })
		else {
			feedAdapter.setNewList(listOf())
			errorAdapter.setNewList(listOf(CustomTextRecyclerItem(R.string.nothing_marked_favorite.resStr())))
		}
		refreshOne?.hideIndicator()
	}

	override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
		DragDropUtil.onMove(feedAdapter, oldPosition, newPosition)
		Collections.swap(feeds, oldPosition, newPosition)
		feeds?.let { Database.allFavorites = it.toTypedArray() }
		context.sendBroadcast(Intent("feed_state"))
		return true
	}

	override fun itemTouchDropped(p0: Int, p1: Int) {
	}

}