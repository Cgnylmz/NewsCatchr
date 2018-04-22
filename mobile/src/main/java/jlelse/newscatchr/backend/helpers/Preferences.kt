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

package jlelse.newscatchr.backend.helpers

import android.content.SharedPreferences
import androidx.core.content.edit
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.extensions.sharedPref
import jlelse.readit.R

/**
 * Preferences
 * For all configurations
 */
object Preferences {

	private fun write(write: (SharedPreferences.Editor) -> Unit) = sharedPref().edit { write(this) }

	private fun read(): SharedPreferences = sharedPref()

	val customTabs: Boolean
		get() = read().getBoolean(R.string.prefs_key_custom_tabs.resStr(), true)

	val amp: Boolean
		get() = read().getBoolean(R.string.prefs_key_amp.resStr(), true)

	val urlShortener: Boolean
		get() = read().getBoolean(R.string.prefs_key_url_shortener.resStr(), true)

	var textScaleFactor: Float
		get() = read().getFloat("textScaleFactor", 1.0f)
		set(value) = write { e -> e.putFloat("textScaleFactor", value) }

	val syncEnabled: Boolean
		get() = read().getBoolean(R.string.prefs_key_sync.resStr(), false)

	var syncInterval: Int
		get() = read().getInt(R.string.prefs_key_sync_interval.resStr(), 30)
		set(value) = write { e -> e.putInt(R.string.prefs_key_sync_interval.resStr(), value) }

	var lastSync: Long
		get() = read().getLong("lastSync", 0.toLong())
		set(value) = write { e -> e.putLong("lastSync", value) }

	var tutorial: Boolean
		get() = read().getBoolean("tutorial", false)
		set(value) = write { e -> e.putBoolean("tutorial", value) }

	val showRecentFeeds: Boolean
		get() = read().getBoolean(R.string.prefs_key_show_recent_feeds.resStr(), true)

	val showRecommendedFeeds: Boolean
		get() = read().getBoolean(R.string.prefs_key_show_recommended_feeds.resStr(), true)

}
