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
import android.graphics.Color
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.loaders.FeedlyLoader
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.layout.RefreshRecyclerUI
import jlelse.newscatchr.ui.recycleritems.ArticleRecyclerItem
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.*

@SuppressLint("ViewConstructor")
class FeedView(val feed: Feed) : ViewManagerView() {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.refreshrecyclerview_recycler) }
	private val articleAdapter = ItemAdapter<ArticleRecyclerItem>()
	//private val footerAdapter = FooterAdapter<ProgressItem>()
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.refreshrecyclerview_refresh) }
	private var articles = mutableListOf<Article>()
	private val favorite
		get() = Database.isSavedFavorite(feed.url())
	private var feedlyLoader: FeedlyLoader? = null
	private var editMenuItem: MenuItem? = null
	private var continuation: String? = null
	private var ranked: String? = null

	override fun onCreateView(): View? {
		super.onCreateView()
		fragmentView = RefreshRecyclerUI().createView(AnkoContext.create(context, this))
		refreshOne?.setOnRefreshListener { loadArticles() }
		//if (recyclerOne?.adapter == null) recyclerOne?.adapter = footerAdapter.wrap(fastAdapter)
		if (recyclerOne?.adapter == null) {
			val adapter: FastAdapter<ArticleRecyclerItem> = FastAdapter.with(articleAdapter)
			recyclerOne?.adapter = adapter
		}
		feedlyLoader = FeedlyLoader().apply {
			type = FeedlyLoader.FeedTypes.FEED
			feedUrl = "feed/" + feed.url()
			continuation = this@FeedView.continuation
			ranked = when (this@FeedView.ranked) {
				"oldest" -> FeedlyLoader.Ranked.OLDEST
				else -> FeedlyLoader.Ranked.NEWEST
			}
		}
		loadArticles(true)
		Database.addLastFeed(feed)
		context.sendBroadcast(Intent("feed_state"))
		return fragmentView
	}

	private fun loadArticles(cache: Boolean = false) = async {
		refreshOne?.showIndicator()
		if (articles.isEmpty() || !cache) await {
			feedlyLoader?.items(cache)?.let {
				articles = it.toMutableList()
			}
			continuation = feedlyLoader?.continuation
		}
		if (articles.notNullAndEmpty()) {
			recyclerOne?.clearOnScrollListeners()
			articleAdapter.setNewList(articles.map { ArticleRecyclerItem(article = it, fragment = this@FeedView) })
			/*recyclerOne?.addOnScrollListener(object : EndlessRecyclerOnScrollListener(footerAdapter) {
				override fun onLoadMore(currentPage: Int) {
					async {
						val newArticles = await { feedlyLoader?.moreItems() }
						continuation = feedlyLoader?.continuation
						if (newArticles != null) {
							articles.addAll(newArticles)
							fastAdapter.add(newArticles.map { ArticleRecyclerItem(article = it, fragment = this@FeedView) })
						}
					}
				}
			})*/
		} else context.nothingFound {
			closeView()
		}
		refreshOne?.hideIndicator()
	}

	fun createHomeScreenShortcut(title: String, feedId: String) {
		Intent().apply {
			@Suppress("DEPRECATION")
			putExtras(bundleOf("duplicate" to false, Intent.EXTRA_SHORTCUT_INTENT to context.intentFor<MainActivity>("feedtitle" to title, "feedid" to feedId).newTask().clearTop(), Intent.EXTRA_SHORTCUT_NAME to title, Intent.EXTRA_SHORTCUT_ICON_RESOURCE to Intent.ShortcutIconResource.fromContext(context.applicationContext, R.mipmap.ic_launcher)))
			action = "com.android.launcher.action.INSTALL_SHORTCUT"
		}.let { context.applicationContext.sendBroadcast(it) }
	}

	override fun inflateMenu(inflater: MenuInflater, menu: Menu?) {
		super.inflateMenu(inflater, menu)
		inflater.inflate(R.menu.feedfragment, menu)
		menu?.findItem(R.id.favorite)?.icon = (if (favorite) R.drawable.ic_favorite_universal else R.drawable.ic_favorite_border_universal).resDrw(context, Color.WHITE)
		editMenuItem = menu?.findItem(R.id.edit_title)
		editMenuItem?.isVisible = favorite
	}

	override fun onOptionsItemSelected(item: MenuItem?) {
		when (item?.itemId) {
			R.id.favorite -> {
				if (!favorite) Database.addFavorites(feed)
				else Database.deleteFavorite(feed.url())
				item.icon = (if (favorite) R.drawable.ic_favorite_universal else R.drawable.ic_favorite_border_universal).resDrw(context, Color.WHITE)
				editMenuItem?.isVisible = favorite
			}
			R.id.sort -> {
				MaterialDialog.Builder(context)
						.title(R.string.sort)
						.items(R.string.newest_first.resStr(), R.string.oldest_first.resStr())
						.itemsCallbackSingleChoice(when (ranked) {
							"oldest" -> 1
							else -> 0
						}) { _, _, which, _ ->
							feedlyLoader?.apply {
								continuation = ""
								ranked = when (which) {
									1 -> FeedlyLoader.Ranked.OLDEST
									else -> FeedlyLoader.Ranked.NEWEST
								}
							}
							articles.clear()
							loadArticles()
							ranked = when (feedlyLoader?.ranked) {
								FeedlyLoader.Ranked.OLDEST -> "oldest"
								else -> "newest"
							}
							true
						}
						.positiveText(R.string.set)
						.negativeText(android.R.string.cancel)
						.show()
			}
			R.id.search -> {
				val progressDialog = context.progressDialog()
				MaterialDialog.Builder(context)
						.title(android.R.string.search_go)
						.input(null, null, { _, query ->
							async {
								progressDialog.show()
								val foundArticles = await {
									FeedlyLoader().apply {
										type = FeedlyLoader.FeedTypes.SEARCH
										feedUrl = "feed/" + feed.url()
										this.query = query.toString()
									}.items(false)
								}
								progressDialog.dismiss()
								if (foundArticles.notNullAndEmpty()) openView(ArticleSearchResultView(articles = foundArticles!!).withTitle("Results for " + query.toString()))
								else context.nothingFound()
							}
						})
						.negativeText(android.R.string.cancel)
						.positiveText(android.R.string.search_go)
						.show()
			}
			R.id.refresh -> loadArticles()
			R.id.edit_title -> {
				MaterialDialog.Builder(context)
						.title(R.string.edit_feed_title)
						.input(null, feed.title, { _, input ->
							if (!input.toString().isNullOrBlank()) {
								Database.updateFavoriteTitle(feed.url(), input.toString())
								feed.title = input.toString()
								title = feed.title
								val curActivity = context
								if (curActivity is MainActivity) curActivity.refreshFragmentDependingTitle(this)
							}
						})
						.negativeText(android.R.string.cancel)
						.positiveText(android.R.string.ok)
						.show()
			}
			R.id.create_shortcut -> {
				createHomeScreenShortcut(title ?: R.string.app_name.resStr()!!, feed.url() ?: "")
				Snackbar.make(contentView, R.string.shortcut_created, Snackbar.LENGTH_SHORT).show()
			}
		}
	}
}
