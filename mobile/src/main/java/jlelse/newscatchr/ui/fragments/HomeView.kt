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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v7.widget.RecyclerView
import android.view.View
import co.metalab.asyncawait.async
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.database
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.extensions.searchForFeeds
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.interfaces.FragmentManipulation
import jlelse.newscatchr.ui.layout.HomeViewUI
import jlelse.newscatchr.ui.recycleritems.*
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

@SuppressLint("ViewConstructor")
class HomeView : ViewManagerView(), FAB, FragmentManipulation {
	private var recFeeds: Array<Feed>? = null
	private var recRelated: Array<String>? = null
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerone) }

	private val lastHeaderAdapter = ItemAdapter<HeaderRecyclerItem>()
	private val lastAdapter = ItemAdapter<FeedRecyclerItem>()
	private val lastFooterAdapter = ItemAdapter<MoreRecyclerItem>()
	private val favoriteHeaderAdapter = ItemAdapter<HeaderRecyclerItem>()
	private val favoriteAdapter = ItemAdapter<FeedRecyclerItem>()
	private val favoriteFooterAdapter = ItemAdapter<MoreRecyclerItem>()
	private val recommendedHeaderAdapter = ItemAdapter<HeaderRecyclerItem>()
	private val recommendedAdapter = ItemAdapter<FeedRecyclerItem>()
	private val recommendedFooterAdapter = ItemAdapter<MoreRecyclerItem>()
	private val recommendedTagsAdapter = ItemAdapter<TagsRecyclerItem>()
	private val refresh: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.homefragment_refresh) }
	private var feedStateUpdateReceiver: FeedStateUpdateReceiver? = null
	private var lastFeedReceiverRegistered = false

	override val fabDrawable = R.drawable.ic_search

	override val fabClick = {
		searchForFeeds(context)
	}

	override val expanded = true

	override fun onCreateView(): View? {
		super.onCreateView()
		if (!lastFeedReceiverRegistered) {
			feedStateUpdateReceiver = FeedStateUpdateReceiver(this)
			context.registerReceiver(feedStateUpdateReceiver, IntentFilter("feed_state"))
			lastFeedReceiverRegistered = true
		}
		fragmentView = HomeViewUI().createView(AnkoContext.create(context, this))
		refresh?.setOnRefreshListener { loadAll(false) }
		loadAll(true)
		return fragmentView
	}

	private fun loadAll(cache: Boolean = true) {
		if (recyclerOne?.adapter == null) {
			val adapter: FastAdapter<NCAbstractItem<*, *>> = FastAdapter.with(listOf(lastHeaderAdapter, lastAdapter, lastFooterAdapter, favoriteHeaderAdapter, favoriteAdapter, favoriteFooterAdapter, recommendedHeaderAdapter, recommendedAdapter, recommendedFooterAdapter, recommendedTagsAdapter))
			recyclerOne?.adapter = adapter
		}
		loadLastFeeds()
		loadFavoriteFeeds()
		loadRecommendedFeeds(cache)
	}

	private fun loadLastFeeds() = async {
		lastHeaderAdapter.setNewList(listOf())
		lastAdapter.setNewList(listOf())
		lastFooterAdapter.setNewList(listOf())
		if (Preferences.showRecentFeeds) {
			val lastFeeds = await { database.allLastFeeds.takeLast(5).reversed() }
			if (lastFeeds.notNullAndEmpty()) {
				lastHeaderAdapter.add(HeaderRecyclerItem(title = R.string.last_feeds.resStr()!!))
				lastAdapter.add(lastFeeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, fragment = this@HomeView, isLast = i == lastFeeds.lastIndex) })
				lastFooterAdapter.add(MoreRecyclerItem { openView(FeedListView(feeds = database.allLastFeeds.reversed().toTypedArray()).withTitle(R.string.last_feeds.resStr())) })
			}
		}
	}

	private fun loadFavoriteFeeds() = async {
		favoriteHeaderAdapter.setNewList(listOf())
		favoriteAdapter.setNewList(listOf())
		favoriteFooterAdapter.setNewList(listOf())
		val favoriteFeeds = await { database.allFavorites.take(5) }
		if (favoriteFeeds.notNullAndEmpty()) {
			favoriteHeaderAdapter.add(HeaderRecyclerItem(title = R.string.favorites.resStr()!!))
			favoriteAdapter.add(favoriteFeeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, fragment = this@HomeView, isLast = i == favoriteFeeds.lastIndex) })
			favoriteFooterAdapter.add(MoreRecyclerItem { openView(FavoritesView().withTitle(R.string.favorites.resStr())) })
		}
	}

	private fun loadRecommendedFeeds(cache: Boolean = false) = async {
		recommendedHeaderAdapter.setNewList(listOf())
		recommendedAdapter.setNewList(listOf())
		recommendedFooterAdapter.setNewList(listOf())
		recommendedTagsAdapter.setNewList(listOf())
		if (Preferences.showRecommendedFeeds) {
			refresh?.showIndicator()
			await {
				if (recFeeds == null || !cache) Feedly().recommendedFeeds(cache = cache) { feeds, related ->
					recFeeds = feeds
					recRelated = related
				}
			}
			val tempRecFeeds = recFeeds?.take(15)
			if (recFeeds.notNullAndEmpty() && tempRecFeeds != null) {
				recommendedHeaderAdapter.add(HeaderRecyclerItem(title = R.string.recommendations.resStr()!!))
				recommendedAdapter.add(tempRecFeeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, fragment = this@HomeView, isLast = i == tempRecFeeds.lastIndex) })
				recommendedFooterAdapter.add(listOf(MoreRecyclerItem {
					openView(FeedListView(feeds = recFeeds, tags = recRelated?.toList()).withTitle(R.string.recommendations.resStr()))
				}))
			}
			if (recRelated.notNullAndEmpty()) recommendedTagsAdapter.add(TagsRecyclerItem(recRelated?.toList(), this@HomeView))
			refresh?.hideIndicator()
		}
	}

	override fun onDestroy() {
		tryOrNull { context.unregisterReceiver(feedStateUpdateReceiver) }
		super.onDestroy()
	}

	private class FeedStateUpdateReceiver(val homeView: HomeView) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			homeView.loadAll()
		}
	}
}