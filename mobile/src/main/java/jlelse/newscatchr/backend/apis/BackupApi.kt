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

package jlelse.newscatchr.backend.apis

import android.content.ClipData
import android.content.Context
import android.support.design.widget.Snackbar
import co.metalab.asyncawait.async
import com.afollestad.ason.Ason
import com.afollestad.materialdialogs.MaterialDialog
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.ObjectStoreDatabase
import jlelse.newscatchr.extensions.progressDialog
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.newscatchr.mainAcivity
import jlelse.readit.R
import org.jetbrains.anko.clipboardManager

class BackupApi(val context: Context) {

	fun backup() = async {
		val progressDialog = context.progressDialog().apply { show() }
		val backupAson = Ason()
		val favorites = await { Ason.serializeArray<Feed>(ObjectStoreDatabase.allFavorites) }
		val bookmarks = await { Ason.serializeArray<Article>(ObjectStoreDatabase.allBookmarks) }
		val readUrls = await { Ason.serializeArray<String>(ObjectStoreDatabase.allReadUrls) }
		await { backupAson.put("favorites", favorites).put("bookmarks", bookmarks).put("readUrls", readUrls) }
		progressDialog.dismiss()
		await { tryOrNull { backupAson.toString().uploadHaste() } }.let { key ->
			if (key != null) {
				MaterialDialog.Builder(context)
						.title(R.string.suc_backup)
						.content(R.string.restore_key_desc)
						.input(R.string.restore_key.resStr(), key) { _, _ ->
							context.clipboardManager.primaryClip = ClipData.newPlainText(R.string.restore_key.resStr(), key)
						}
						.negativeText(android.R.string.cancel)
						.positiveText(android.R.string.copy)
						.show()
			} else Snackbar.make(mainAcivity!!.findViewById(R.id.mainactivity_container), R.string.backup_failed, Snackbar.LENGTH_SHORT).show()
		}
	}

	fun restore(key: String) = async {
		await { tryOrNull { key.downloadHaste() } }.let { json ->
			if (json != null) {
				val progressDialog = context.progressDialog().apply { show() }
				tryOrNull { Ason(json) }?.let { restoreAson ->
					await { tryOrNull { ObjectStoreDatabase.allFavorites = restoreAson.get("favorites", Array<Feed>::class.java) } }
					await { tryOrNull { ObjectStoreDatabase.allBookmarks = restoreAson.get("bookmarks", Array<Article>::class.java) } }
					await { tryOrNull { ObjectStoreDatabase.allReadUrls = restoreAson.get("readUrls", Array<String>::class.java) } }
				}
				progressDialog.dismiss()
				MaterialDialog.Builder(context).content(R.string.suc_restore).positiveText(android.R.string.ok).show()
			} else MaterialDialog.Builder(context).content(R.string.restore_failed).positiveText(android.R.string.ok).show()
		}
	}

}

fun Context.backupRestore() {
	MaterialDialog.Builder(this)
			.items(R.string.backup.resStr(), R.string.restore.resStr())
			.itemsCallback { _, _, which, _ ->
				when (which) {
					0 -> BackupApi(this).backup()
					else -> {
						MaterialDialog.Builder(this)
								.title(R.string.restore)
								.input(R.string.restore_key, 0, { _, key ->
									BackupApi(this).restore(key.toString())
								})
								.negativeText(android.R.string.cancel)
								.positiveText(R.string.restore)
								.show()
					}
				}
			}
			.negativeText(android.R.string.cancel)
			.show()
}