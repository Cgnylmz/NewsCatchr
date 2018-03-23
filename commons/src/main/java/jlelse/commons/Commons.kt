package jlelse.commons

import com.afollestad.ason.Ason
import com.github.kittinunf.fuel.core.ResponseDeserializable


class Deserializer<out T : Any>(private val objectClass: Class<T>) : ResponseDeserializable<T> {
	override fun deserialize(content: String): T = Ason.deserialize(content, objectClass)
}

class ListDeserializer<out T : Any>(private val objectClass: Class<T>) : ResponseDeserializable<List<T>> {
	override fun deserialize(content: String): List<T> = Ason.deserializeList(content, objectClass)
}