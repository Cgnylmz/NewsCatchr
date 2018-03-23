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
import android.support.v7.widget.RecyclerView
import android.view.View
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.ui.layout.BasicRecyclerUI
import jlelse.newscatchr.ui.recycleritems.FeedRecyclerItem
import jlelse.newscatchr.ui.recycleritems.NCAbstractItem
import jlelse.newscatchr.ui.recycleritems.TagsRecyclerItem
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

@SuppressLint("ViewConstructor")
class FeedListView(val feeds: List<Feed>? = null, val tags: List<String>? = null) : ViewManagerView() {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.basicrecyclerview_recycler) }
	private var feedAdapter = ItemAdapter<FeedRecyclerItem>()
	private var tagsAdapter = ItemAdapter<TagsRecyclerItem>()

	override fun onCreateView(): View? {
		super.onCreateView()
		fragmentView = BasicRecyclerUI().createView(AnkoContext.create(context, this))
		if (recyclerOne?.adapter == null) {
			val adapter: FastAdapter<NCAbstractItem<*, *>> = FastAdapter.with(listOf(tagsAdapter, feedAdapter))
			recyclerOne?.adapter = adapter
		}
		if (feeds?.notNullAndEmpty() == true) feedAdapter.setNewList(feeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, isLast = i == feeds.lastIndex, fragment = this@FeedListView) })
		else feedAdapter.setNewList(listOf())
		if (tags.notNullAndEmpty()) tagsAdapter.setNewList(listOf(TagsRecyclerItem(fragment = this, tags = tags)))
		else tagsAdapter.setNewList(listOf())
		return fragmentView
	}

}
