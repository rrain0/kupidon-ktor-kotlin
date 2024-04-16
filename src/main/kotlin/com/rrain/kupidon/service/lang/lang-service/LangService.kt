package com.rrain.kupidon.service.lang.`lang-service`



enum class Lang(val value: String) {
  enUS("en-US"),
  ruRU("ru-RU"),
  ;
  
  companion object {
    fun getOrDefault(value: String?): Lang {
      return Lang.entries.find { it.value==value } ?: Default
    }
  }
}
val Default = Lang.enUS



abstract class UiValue {
  abstract val lang: Lang
  abstract val value: Any
}

data class UiText(
  override val lang: Lang,
  override val value: String,
) : UiValue()

data class UiTemplate<T : Function<String>>(
  override val lang: Lang,
  override val value: T,
) : UiValue()




fun <T : UiValue>List<T>.pickUiValue(langs: List<Lang>): T {
  return this
    .sortedWith { a,b ->
      val aIdx = langs.indexOf(a.lang)
        .let { if (it==-1) langs.size else it }
      val bIdx = langs.indexOf(b.lang)
        .let { if (it==-1) langs.size else it }
      aIdx - bIdx
    }
    .first()
}




private fun example(){

}