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

package jlelse.newscatchr

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.evernote.android.job.JobManager
import jlelse.newscatchr.backend.helpers.*
import jlelse.newscatchr.ui.activities.MainActivity
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment

/**
 * Application class
 */
class NewsCatchr : Application() {
	override fun onCreate() {
		super.onCreate()
		appContext = applicationContext
		database = ObjectStoreDatabase()
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		JobManager.create(this@NewsCatchr).addJobCreator { tag ->
			when (tag) {
				SyncJob.TAG -> SyncJob()
				else -> null
			}
		}
		if (Preferences.syncEnabled) scheduleSync(Preferences.syncInterval) else cancelSync()
	}
}

lateinit var appContext: Context
var customTabsHelperFragment: CustomTabsHelperFragment? = null
var lastTab = 0
var mainAcivity: MainActivity? = null
lateinit var database: IDatabase