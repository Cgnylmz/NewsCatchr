package jlelse.sourcebase

data class Article(
		var id: String,
		var time: Long? = 0,
		var author: String? = null,
		var title: String? = null,
		var link: String? = null,
		var image: String? = null,
		var feedTitle: String? = null,
		var content: String? = null,
		var amp: String? = null,
		var tags: List<String>? = null
)