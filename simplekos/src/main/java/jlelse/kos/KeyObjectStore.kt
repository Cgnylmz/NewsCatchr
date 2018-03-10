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

package jlelse.kos

import android.content.Context
import com.afollestad.ason.Ason
import com.afollestad.ason.AsonArray
import java.io.File

/**
 * Save objects in files organized by keys
 *
 * @param context Application context
 * @param name Store / Database name, default = "default"
 * @param fileExtension File extension of the database, default = "nc"
 * @param cache Save database to the app's cache directory or otherwise to the files directory, default = false
 *
 */
class KeyObjectStore(context: Context, name: String = "default", private val fileExtension: String = "nc", cache: Boolean = false) {
	private var folder: File = File(if (cache) context.applicationContext?.cacheDir else context.applicationContext?.filesDir, "NCStore$name")

	init {
		tryOrNull(!folder.exists()) { folder.mkdir() }
	}

	/**
	 * Add a new item to the store
	 *
	 * @param key Key for the item, doesn't save when it's null
	 * @param item Item object, deletes existing entries when null
	 *
	 * @return Current KeyObjectStore
	 */
	fun <T> write(key: String?, item: Any?): KeyObjectStore {
		if (item == null) delete(key)
		else if (key != null && key.isNotBlank()) {
			val file = File(folder, "$key.$fileExtension")
			if (item is Array<*>) tryOrNull { saveAsonArray(file, Ason.serializeArray<T>(item)) }
			else tryOrNull { saveAson(file, Ason.serialize(item)) }
		}
		return this
	}

	/**
	 * Delete existing entries by the specified key
	 *
	 * @param key Key for the item, that should be deleted
	 *
	 * @return Current KeyObjectStore
	 */
	fun delete(key: String?): KeyObjectStore {
		tryOrNull(key != null && key.isNotBlank()) { File(folder, "$key.$fileExtension").delete() }
		return this
	}

	/**
	 * Retrieve items for the store
	 *
	 * @param key Key for the item
	 * @param type Class of the item
	 * @param defaultValue Default value, default = null
	 *
	 * @return Queried item or null
	 */
	fun <T> read(key: String?, type: Class<T>, defaultValue: T? = null): T? = if (key != null) tryOrNull {
		val file = File(folder, "$key.$fileExtension")
		if (file.exists()) {
			if (type.isArray) Ason.deserialize(AsonArray<T>(file.readText()), type)
			else Ason.deserialize(Ason(file.readText()), type)
		} else null
	} ?: defaultValue else defaultValue

	/**
	 * Destroy the store
	 */
	fun destroy() {
		tryOrNull { folder.deleteRecursively() }
	}

	/**
	 * Check if key already exists
	 *
	 * @param key Key to check
	 *
	 * @return True if key exists
	 */
	fun exists(key: String?): Boolean = if (key != null && key.isNotBlank()) File(folder, "$key.$fileExtension").exists() else false

	private fun saveAson(file: File, ason: Ason) {
		if (!file.exists()) tryOrNull { file.createNewFile() }
		tryOrNull { file.writeText(ason.toString()) }
	}

	private fun <T> saveAsonArray(file: File, ason: AsonArray<T>) {
		if (!file.exists()) tryOrNull { file.createNewFile() }
		tryOrNull { file.writeText(ason.toString()) }
	}

	private fun <T> tryOrNull(execute: Boolean = true, code: () -> T): T? = try {
		if (execute) code() else null
	} catch (e: Exception) {
		null
	}

}