package com.rrain.kupidon.libs.`ui-text`


abstract class UiValue<L : Enum<L>> {
  abstract val lang: L
  abstract val value: Any
}

data class UiText<L : Enum<L>>(
  override val lang: L,
  override val value: String,
) : UiValue<L>()

data class UiTemplate<L : Enum<L>, T : Function<String>>(
  override val lang: L,
  override val value: T,
) : UiValue<L>()




fun <L : Enum<L>, T : UiValue<L>>List<T>.pickUiValue(langs: List<L>): T {
  return this
    .sortedWith { a, b ->
      val aIdx = langs.indexOf(a.lang)
        .let { if (it == -1) langs.size else it }
      val bIdx = langs.indexOf(b.lang)
        .let { if (it == -1) langs.size else it }
      aIdx - bIdx
    }
    .first()
}
