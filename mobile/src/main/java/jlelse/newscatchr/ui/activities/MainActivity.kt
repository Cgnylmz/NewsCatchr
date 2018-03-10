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

package jlelse.newscatchr.ui.activities

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import androidx.view.isInvisible
import androidx.view.isVisible
import com.afollestad.materialdialogs.MaterialDialog
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.openUrl
import jlelse.newscatchr.backend.apis.share
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.customTabsHelperFragment
import jlelse.newscatchr.extensions.resDrw
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.extensions.searchForFeeds
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.newscatchr.lastTab
import jlelse.newscatchr.mainAcivity
import jlelse.newscatchr.ui.fragments.BookmarksView
import jlelse.newscatchr.ui.fragments.FeedView
import jlelse.newscatchr.ui.fragments.HomeView
import jlelse.newscatchr.ui.fragments.SettingsView
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.interfaces.FragmentManipulation
import jlelse.newscatchr.ui.layout.MainActivityUI
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerActivity
import jlelse.viewmanager.ViewManagerView
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import org.jetbrains.anko.find
import org.jetbrains.anko.setContentView

class MainActivity : ViewManagerActivity() {
	private val toolbar: Toolbar? by lazy { find<Toolbar>(R.id.mainactivity_toolbar) }
	private val appbar: AppBarLayout? by lazy { find<AppBarLayout>(R.id.mainactivity_appbar) }
	private val fab: FloatingActionButton? by lazy { find<FloatingActionButton>(R.id.mainactivity_fab) }
	private val subtitle: TextView? by lazy { find<TextView>(R.id.mainactivity_toolbarsubtitle) }
	val bottomNavigationView: BottomNavigationView? by lazy { find<BottomNavigationView>(R.id.mainactivity_navigationview) }

	override val initViewStacks: MutableList<MutableList<ViewManagerView>>
		get() = mutableListOf(
				mutableListOf(HomeView().withTitle(R.string.news.resStr())),
				mutableListOf(BookmarksView().withTitle(R.string.bookmarks.resStr())),
				mutableListOf(SettingsView().withTitle(R.string.settings.resStr()))
		)
	override val containerView: FrameLayout
		get() = find(R.id.mainactivity_container)

	override fun onCreate(savedInstanceState: Bundle?) {
		mainAcivity = this
		MainActivityUI().setContentView(this)
		super.onCreate(savedInstanceState)
		customTabsHelperFragment = CustomTabsHelperFragment.attachTo(this@MainActivity)
		setSupportActionBar(toolbar)
		bottomNavigationView?.apply {
			selectedItemId = when (lastTab) {
				1 -> R.id.bb_bookmarks
				2 -> R.id.bb_settings
				else -> R.id.bb_news
			}
			setOnNavigationItemSelectedListener { item ->
				val itemNumber = when (item.itemId) {
					R.id.bb_news -> 0
					R.id.bb_bookmarks -> 1
					R.id.bb_settings -> 2
					else -> 0
				}
				switchStack(itemNumber)
				lastTab = itemNumber
				true
			}
			setOnNavigationItemReselectedListener {
				resetStack()
			}
		}
		checkFragmentDependingThings()
		handleIntent(intent)
		switchStack(currentStack())
		if (!Preferences.tutorial) showTutorial()
	}

	override fun onSwitchView() {
		super.onSwitchView()
		checkFragmentDependingThings()
	}

	private fun handleIntent(intent: Intent?) {
		if (intent != null) {
			// Shortcut
			intent.getStringExtra("feedid")?.let {
				resetStack()
				val feedTitle = intent.getStringExtra("feedtitle")
				if (!it.isBlank()) openView(FeedView(feed = Feed(feedId = it, title = feedTitle)).withTitle(feedTitle))
			}
			// Browser
			if (intent.scheme == "http" || intent.scheme == "https") {
				intent.dataString?.let {
					MaterialDialog.Builder(this)
							.items(R.string.search_for_feeds.resStr(), R.string.this_is_article.resStr())
							.itemsCallback { _, _, i, _ ->
								when (i) {
									0 -> searchForFeeds(this, it)
									1 -> it.openUrl(this@MainActivity, notAmpLink = it)
								}
							}
							.show()
				}
			}
			// Google Voice Search
			if (intent.action == "com.google.android.gms.actions.SEARCH_ACTION") {
				intent.getStringExtra(SearchManager.QUERY).let {
					searchForFeeds(this, it)
				}
			}
		}
	}

	private fun checkFragmentDependingThings() {
		val currentFragment = currentView()
		// Check Back Arrow
		if (isRootView()) {
			supportActionBar?.setDisplayHomeAsUpEnabled(false)
			appbar?.setExpanded(if (currentFragment is FragmentManipulation) currentFragment.expanded
					?: false else false)
		} else {
			supportActionBar?.setDisplayHomeAsUpEnabled(true)
			appbar?.setExpanded(if (currentFragment is FragmentManipulation) currentFragment.expanded
					?: true else true)
		}
		// Check Title
		refreshFragmentDependingTitle(currentFragment)
		// Check Help Menu Item
		invalidateOptionsMenu()
		// Check FAB
		if (currentFragment is FAB) {
			fab?.let {
				if (currentFragment.fabDrawable != null) it.setImageDrawable(currentFragment.fabDrawable?.resDrw(this, Color.WHITE))
				it.setOnClickListener { currentFragment.fabClick() }
				it.isVisible = true
				it.show()
			}
		} else {
			fab?.isInvisible = true
		}
	}

	fun refreshFragmentDependingTitle(fragment: ViewManagerView?) = tryOrNull {
		toolbar?.title = R.string.app_name.resStr()
		subtitle?.text = fragment?.title
	}

	fun showTutorial() {
		FancyShowCaseQueue()
				.add(FancyShowCaseView.Builder(this)
						.title(R.string.tutorial_0.resStr())
						.build())
				.add(FancyShowCaseView.Builder(this)
						.focusOn(bottomNavigationView?.find(R.id.bb_news))
						.title(R.string.tutorial_1.resStr())
						.build())
				.add(FancyShowCaseView.Builder(this)
						.focusOn(bottomNavigationView?.find(R.id.bb_bookmarks))
						.title(R.string.tutorial_2.resStr())
						.build())
				.add(FancyShowCaseView.Builder(this)
						.focusOn(bottomNavigationView?.find(R.id.bb_settings))
						.title(R.string.tutorial_3.resStr())
						.build())
				.add(FancyShowCaseView.Builder(this)
						.focusOn(fab)
						.title(R.string.tutorial_4.resStr())
						.build())
				.add(FancyShowCaseView.Builder(this)
						.title(R.string.tutorial_5.resStr())
						.build())
				.show()
		Preferences.tutorial = true
	}

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		handleIntent(intent)
	}

	override fun createMenu(menu: Menu?) {
		super.createMenu(menu)
		menuInflater.inflate(R.menu.universal, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		super.onOptionsItemSelected(item)
		when (item?.itemId) {
			android.R.id.home -> onBackPressed()
			R.id.share_app -> share("\" ${R.string.share_app.resStr()}\"", R.string.try_nc.resStr()!!)
		}
		return true
	}

	override fun onBackPressed() = if (isRootView()) super.onBackPressed() else closeView()

}
