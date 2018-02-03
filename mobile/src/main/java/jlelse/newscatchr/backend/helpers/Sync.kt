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

import android.content.Context
import android.content.Intent
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import jlelse.newscatchr.appContext
import jlelse.newscatchr.backend.loaders.FeedlyLoader
import jlelse.newscatchr.backend.loaders.ILoader
import jlelse.newscatchr.database
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.tryOrNull

fun scheduleSync(intervalMins: Int) {
	val intervalMs = (intervalMins * 60000).toLong()
	val allJobs = JobManager.instance().getAllJobRequestsForTag(SyncJob.TAG)
	val oldIntervalMs = if (allJobs.notNullAndEmpty()) allJobs.first().intervalMs else 0.toLong()
	if (oldIntervalMs == 0.toLong() || oldIntervalMs != intervalMs || allJobs.isEmpty()) {
		JobRequest.Builder(SyncJob.TAG)
				.setPeriodic(intervalMs)
				.setUpdateCurrent(true)
				.build()
				.schedule()
	}
}

fun cancelSync() {
	JobManager.instance().cancelAllForTag(SyncJob.TAG)
}

class SyncJob : Job() {
	override fun onRunJob(params: Params): Result {
		return if (sync(context) != null) Result.SUCCESS else Result.RESCHEDULE
	}

	companion object {
		val TAG = "sync_job_tag"
	}
}

fun sync(context: Context): String? = tryOrNull {
	System.out.println("Sync started")
	// AppContext and database are probably not initialized yet
	appContext = context.applicationContext
	database = ObjectStoreDatabase()
	database.allFavorites.forEach {
		FeedlyLoader().apply {
			type = ILoader.FeedTypes.FEED
			feedUrl = "feed/" + it.url()
			ranked = ILoader.Ranked.NEWEST
		}.items(false)
	}
	System.out.println("Sync finished")
	Preferences.lastSync = System.currentTimeMillis()
	context.sendBroadcast(Intent("syncStatus"))
	"not null"
}