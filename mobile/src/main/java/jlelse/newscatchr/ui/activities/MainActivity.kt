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
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.fetchArticle
import jlelse.newscatchr.backend.apis.share
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.customTabsHelperFragment
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.lastTab
import jlelse.newscatchr.mainAcivity
import jlelse.newscatchr.ui.fragments.*
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.interfaces.FragmentManipulation
import jlelse.newscatchr.ui.layout.MainActivityUI
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerActivity
import jlelse.viewmanager.ViewManagerView
import me.toptas.fancyshowcase.FancyShowCaseQueue
import me.toptas.fancyshowcase.FancyShowCaseView
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.setContentView

class MainActivity : ViewManagerActivity() {
	private val toolbar: Toolbar? by lazy { find<Toolbar>(R.id.mainactivity_toolbar) }
	private val appbar: AppBarLayout? by lazy { find<AppBarLayout>(R.id.mainactivity_appbar) }
	private val fab: FloatingActionButton? by lazy { find<FloatingActionButton>(R.id.mainactivity_fab) }
	private val subtitle: TextView? by lazy { find<TextView>(R.id.mainactivity_toolbarsubtitle) }
	val bottomNavigationView: BottomNavigationView? by lazy { find<BottomNavigationView>(R.id.mainactivity_navigationview) }
	private var billingProcessor: BillingProcessor? = null

	var IABReady = false

	private val licenceKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmnVnCUmnyl0MQek4LpMopUVNb5czY+7RVsCl0vV7LU2nAJppZqTtHoZpeFyD9ae6BtVx/f6WW/xV37KCr4Eo/4Bh796e8AYzxfOm8icw7TPU9M2FNUjJ46qtZZl5I9ItfhOoapu5tGAY8i0Z5142046UpMs0XLGeGhVsr/3RSGWSzbHRhWOKVFJqa1JgWSHUGTUUHvkVai3sWKl1acreIivio3kpNh/jY9T9xwd6pl5Xzg32i00m87BMuJaA+QofQjFWTFmsUDC0tx+nERxnUud6S/A/n2nKKkhQ3c0mz961swxarWpzrP131VbYYPmGZ0WhXt6tMsTnpg/G++2l1wIDAQAB"
	private val PRO_SKU_1 = "prosub"
	private val PRO_SKU_2 = "prosub2"
	private val PRO_SKU_3 = "prosub3"
	private val PRO_SKU_4 = "prosub4"
	private val DONATION_SKU_1 = "prodonation1"
	private val DONATION_SKU_2 = "prodonation2"
	private val DONATION_SKU_3 = "prodonation3"
	private val DONATION_SKU_4 = "prodonation4"

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
		doAsync {
			// Init Custom Tabs
			customTabsHelperFragment = CustomTabsHelperFragment.attachTo(this@MainActivity)
			// Check purchases
			if (BillingProcessor.isIabServiceAvailable(this@MainActivity)) {
				billingProcessor = BillingProcessor(this@MainActivity, licenceKey, object : BillingProcessor.IBillingHandler {
					override fun onBillingInitialized() {
						IABReady = true
						billingProcessor?.loadOwnedPurchasesFromGoogle()
					}

					override fun onProductPurchased(productId: String, details: TransactionDetails?) {
						MaterialDialog.Builder(this@MainActivity)
								.title(R.string.thanks_purchase)
								.content(R.string.thanks_purchase_desc)
								.positiveText(android.R.string.ok)
								.show()
					}

					override fun onBillingError(errorCode: Int, error: Throwable?) {}
					override fun onPurchaseHistoryRestored() {}
				})
			}
		}
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
									1 -> async {
										val progressDialog = this@MainActivity.progressDialog().apply { show() }
										val article = await { tryOrNull { it.fetchArticle() } }
										if (article != null) {
											this@MainActivity.openView(ArticleView(article = article).withTitle(article.title))
										}
										progressDialog.dismiss()
									}
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
			// Pocket
			val currentFrag = currentView()
			if (currentFrag is SettingsView && intent.scheme == "pocketapp45699") currentFrag.finishPocketAuth()
		}
	}

	private fun checkFragmentDependingThings() {
		val currentFragment = currentView()
		// Check Back Arrow
		if (isRootView()) {
			supportActionBar?.setDisplayHomeAsUpEnabled(false)
			appbar?.setExpanded(if (currentFragment is FragmentManipulation) currentFragment.expanded ?: false else false)
		} else {
			supportActionBar?.setDisplayHomeAsUpEnabled(true)
			appbar?.setExpanded(if (currentFragment is FragmentManipulation) currentFragment.expanded ?: true else true)
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
				it.showView()
				it.show()
			}
		} else {
			fab?.makeInvisible()
		}
	}

	fun refreshFragmentDependingTitle(fragment: ViewManagerView?) = tryOrNull {
		toolbar?.title = R.string.app_name.resStr()
		subtitle?.text = fragment?.title
	}

	fun getProOptions() = billingProcessor?.getSubscriptionListingDetails(arrayListOf(PRO_SKU_1, PRO_SKU_2, PRO_SKU_3, PRO_SKU_4))?.map { "${it.title}: ${it.priceText} ${it.subscriptionPeriod}" }

	fun purchaseProSub(number: Int) {
		billingProcessor?.subscribe(this, when (number) {
			1 -> PRO_SKU_2
			2 -> PRO_SKU_3
			3 -> PRO_SKU_4
			else -> PRO_SKU_1
		})
	}

	fun getDonationOptions() = billingProcessor?.getPurchaseListingDetails(arrayListOf(DONATION_SKU_1, DONATION_SKU_2, DONATION_SKU_3, DONATION_SKU_4))?.map { "${it.title}: ${it.priceText}" }

	fun purchaseDonation(number: Int) {
		val product = when (number) {
			1 -> DONATION_SKU_2
			2 -> DONATION_SKU_3
			3 -> DONATION_SKU_4
			else -> DONATION_SKU_1
		}
		if (billingProcessor?.isPurchased(product) == true) billingProcessor?.consumePurchase(product)
		billingProcessor?.purchase(this, product)
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

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (billingProcessor?.handleActivityResult(requestCode, resultCode, data) != true) super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onDestroy() {
		billingProcessor?.release()
		super.onDestroy()
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
