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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import jlelse.newscatchr.appContext
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.backupRestore
import jlelse.newscatchr.backend.apis.openUrl
import jlelse.newscatchr.backend.helpers.*
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.mainAcivity
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
	private var settingsContext: Context = context ?: mainAcivity ?: appContext!!

	private var syncReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			this@SettingsFragment.refreshLastSyncTime()
		}
	}
	private var syncReceiverRegistered = false

	private val clearCachePref: Preference? by lazy { findPreference(R.string.prefs_key_clear_cache.resStr()) }
	private val clearHistoryPref: Preference? by lazy { findPreference(R.string.prefs_key_clear_history.resStr()) }
	private val viewLibsPref: Preference? by lazy { findPreference(R.string.prefs_key_view_libs.resStr()) }
	private val viewApisPref: Preference? by lazy { findPreference(R.string.prefs_key_view_apis.resStr()) }
	private val aboutPref: Preference? by lazy { findPreference(R.string.prefs_key_about_nc.resStr()) }
	private val backupPref: Preference? by lazy { findPreference(R.string.prefs_key_backup.resStr()) }
	private val importPref: Preference? by lazy { findPreference(R.string.prefs_key_import_opml.resStr()) }
	private val syncNowPref: Preference? by lazy { findPreference(R.string.prefs_key_sync_now.resStr()) }
	private val syncPref: Preference? by lazy { findPreference(R.string.prefs_key_sync.resStr()) }
	private val syncIntervalPref: Preference? by lazy { findPreference(R.string.prefs_key_sync_interval.resStr()) }
	private val issuePref: Preference? by lazy { findPreference(R.string.prefs_key_issue.resStr()) }
	private val showTutorialPref: Preference? by lazy { findPreference(R.string.prefs_key_show_tutorial.resStr()) }

	override fun onCreatePreferences(p0: Bundle?, p1: String?) {
		addPreferencesFromResource(R.xml.preferences)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = super.onCreateView(inflater, container, savedInstanceState)
		if (!syncReceiverRegistered) {
			settingsContext.registerReceiver(syncReceiver, IntentFilter("syncStatus"))
			syncReceiverRegistered = true
		}
		return view
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		// Add ClickListeners
		clearCachePref?.onPreferenceClickListener = this
		clearHistoryPref?.onPreferenceClickListener = this
		viewLibsPref?.onPreferenceClickListener = this
		viewApisPref?.onPreferenceClickListener = this
		aboutPref?.onPreferenceClickListener = this
		backupPref?.onPreferenceClickListener = this
		importPref?.onPreferenceClickListener = this
		syncIntervalPref?.onPreferenceClickListener = this
		syncNowPref?.onPreferenceClickListener = this
		issuePref?.onPreferenceClickListener = this
		showTutorialPref?.onPreferenceClickListener = this

		// Add ChangeListeners
		syncPref?.onPreferenceChangeListener = this

		refreshLastSyncTime()
		refreshSyncIntervalDesc()
	}

	override fun onPreferenceClick(preference: Preference?): Boolean {
		when (preference) {
			clearCachePref -> {
				settingsContext.clearCache {
					Snackbar.make(mainAcivity!!.findViewById(R.id.mainactivity_container), R.string.cleared_cache, Snackbar.LENGTH_SHORT).show()
				}
			}
			clearHistoryPref -> {
				doAsync {
					Database.allLastFeeds = arrayOf()
					uiThread {
						settingsContext.sendBroadcast(Intent("feed_state"))
						Snackbar.make(mainAcivity!!.findViewById(R.id.mainactivity_container), R.string.cleared_history, Snackbar.LENGTH_SHORT).show()
					}
				}
			}
			viewLibsPref -> {
				val html = listOf(
						Library("Material Dialogs", "A beautiful, fluid, and customizable dialogs API.", "https://github.com/afollestad/material-dialogs"),
						Library("FastAdapter", "The bullet proof, fast and easy to use adapter library, which minimizes developing time to a fraction...", "https://github.com/mikepenz/FastAdapter/"),
						Library("jsoup", "Java HTML Parser, with best of DOM, CSS, and jquery", "https://github.com/jhy/jsoup"),
						Library("Bridge", "A simple but powerful HTTP networking library for Java. It features a Fluent chainable API, powered by Java URLConnection classes for maximum compatibility and speed.", "https://github.com/afollestad/bridge"),
						Library("Ason", "JSON in Java made easy! Catch less exceptions, serialize/deserialize with ease, plus some other useful tricks and conveniences!", "https://github.com/afollestad/ason"),
						Library("Glide", "An image loading and caching library for Android focused on smooth scrolling", "https://github.com/bumptech/glide"),
						Library("FlexboxLayout", "FlexboxLayout is a library project which brings the similar capabilities of CSS Flexible Box Layout Module to Android.", "https://github.com/google/flexbox-layout"),
						Library("Android-Job", "Android library to handle jobs in the background.", "https://github.com/evernote/android-job"),
						Library("CustomTabsHelper", "Custom tabs, made easy.", "https://github.com/DreaminginCodeZH/CustomTabsHelper"),
						Library("Async/Await", "async/await for Android built upon coroutines introduced in Kotlin 1.1", "https://github.com/metalabdesign/AsyncAwait"),
						Library("Anko", "Pleasant Android application development", "https://github.com/Kotlin/anko"),
						Library("FancyShowCaseView", "An easy-to-use customizable show case view with circular reveal animation.", "https://github.com/faruktoptas/FancyShowCaseView", true)
				).joinToString(separator = "") { "<b><a href=\"${it.link}\">${it.name}</a></b> ${it.description}${if (!it.isLast) "<br><br>" else ""}" }
				MaterialDialog.Builder(settingsContext)
						.title(R.string.used_libraries)
						.content(html.toHtml())
						.positiveText(android.R.string.ok)
						.build()
						.apply {
							contentView?.applyLinks(false)
							tryOrNull { show() }
						}
			}
			viewApisPref -> {
				val html = listOf(
						Library("feedly Cloud API", "", "https://developer.feedly.com"),
						Library("tny.im Url Shortener", "", "https://tny.im/aboutapi.php"),
						Library("Mercury by Postlight", "", "https://mercury.postlight.com/", true)
				).joinToString(separator = "") { "<b><a href=\"${it.link}\">${it.name}</a></b>${if (!it.isLast) "<br><br>" else ""}" }
				MaterialDialog.Builder(settingsContext)
						.title(R.string.used_libraries)
						.content(html.toHtml())
						.positiveText(android.R.string.ok)
						.build()
						.apply {
							contentView?.applyLinks(false)
							tryOrNull { show() }
						}
			}
			aboutPref -> {
				val description = "<b>The best newsreader for Android<br><i>It's the way of reading news in the future</i></b><br><br>Developer: Jan-Lukas Else<br><br><a href=\"https://newscatchr.jlelse.eu\">NewsCatchr Website</a><br><a href=\"https://github.com/jlelse/NewsCatchr-OpenSource\">Source code on GitHub</a><br><br>"
				val statsDesc = "You already opened ${Database.allReadUrls.size} articles. Thanks for that!"
				MaterialDialog.Builder(settingsContext)
						.title(R.string.app_name)
						.content("$description$statsDesc".toHtml())
						.positiveText(android.R.string.ok)
						.build()
						.apply {
							contentView?.applyLinks(false)
							tryOrNull { show() }
						}
			}
			backupPref -> settingsContext.backupRestore()
			importPref -> {
				MaterialDialog.Builder(settingsContext)
						.title(R.string.import_opml)
						.input(R.string.import_opml_hint, 0) { _, input ->
							importOpml(input.toString())
						}
						.positiveText(android.R.string.ok)
						.let { tryOrNull { it.show() } }
			}
			syncIntervalPref -> {
				MaterialDialog.Builder(settingsContext)
						.title(R.string.sync_interval)
						.items(R.array.sync_interval_titles)
						.itemsCallbackSingleChoice(resources.getIntArray(R.array.sync_interval_values).indexOf(Preferences.syncInterval)) { _, _, which, _ ->
							Preferences.syncInterval = resources.getIntArray(R.array.sync_interval_values)[which]
							if (Preferences.syncEnabled) scheduleSync(Preferences.syncInterval) else cancelSync()
							refreshSyncIntervalDesc()
							true
						}
						.let { tryOrNull { it.show() } }
			}
			syncNowPref -> {
				doAsync { sync(settingsContext) }
			}
			issuePref -> "https://github.com/NewsCatchr/NewsCatchr/issues".openUrl(mainAcivity!!, amp = false)
			showTutorialPref -> {
				mainAcivity?.bottomNavigationView?.find<View>(R.id.bb_news)?.performClick()
				mainAcivity?.showTutorial()
			}
		}
		return true
	}

	override fun onPreferenceChange(preference: Preference?, value: Any?): Boolean {
		when (preference) {
			syncNowPref -> if (Preferences.syncEnabled) scheduleSync(Preferences.syncInterval) else cancelSync()
		}
		return true
	}

	private fun refreshLastSyncTime() {
		syncNowPref?.summary = "${R.string.last_suc_sync.resStr()}: " + when (Preferences.lastSync) {
			0.toLong() -> R.string.never.resStr()
			else -> DateUtils.getRelativeTimeSpanString(Preferences.lastSync)
		}
	}

	private fun refreshSyncIntervalDesc() {
		syncIntervalPref?.summary = R.array.sync_interval_titles.resStrArr()!![R.array.sync_interval_values.resIntArr()!!.indexOf(Preferences.syncInterval)]
	}

	private fun importOpml(opml: String?) = async {
		var imported = 0
		var feeds: Array<Feed>?
		if (!opml.isNullOrBlank()) await {
			feeds = opml?.convertOpmlToFeeds()
			feeds?.forEach { Database.addFavorites(it) }
			imported = feeds?.size ?: 0
		}
		settingsContext.sendBroadcast(Intent("feed_state"))
		MaterialDialog.Builder(settingsContext)
				.title(R.string.import_opml)
				.content(if (imported != 0) R.string.suc_import else R.string.import_failed)
				.positiveText(android.R.string.ok)
				.let { tryOrNull { it.show() } }
	}

	override fun onDestroy() {
		tryOrNull { settingsContext.unregisterReceiver(syncReceiver) }
		super.onDestroy()
	}

	private class Library(val name: String, val description: String, val link: String, val isLast: Boolean = false)
}
