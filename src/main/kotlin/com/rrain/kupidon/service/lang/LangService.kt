package com.rrain.kupidon.service.lang




object LangService {
  
  
  val AppLangs = listOf("en-US","ru-RU")
  
  
  fun <T : UiValue>List<T>.prepareUiValues(langs: List<String>): List<T> {
    return this
      .sortedWith { a,b ->
        if (a.value==b.value){
          val langIdxA = langs.indexOf(a.lang)
            .let { if (it==-1) langs.size else it }
          val langIdxB = langs.indexOf(b.lang)
            .let { if (it==-1) langs.size else it }
          langIdxA - langIdxB
        }
        0
      }
      .distinctBy { it.value }
  }
  
  
}


abstract class UiValue {
  abstract val value: String
  abstract val lang: String
  abstract val text: Any
}


data class UiText(
  override val value: String,
  override val lang: String,
  override val text: String,
) : UiValue()

data class UiTemplateText<T : Function<String>>(
  override val value: String,
  override val lang: String,
  override val text: T,
) : UiValue()






private fun example(){
  
  data class SomeTxtParams(
    val name: String,
    val prop: String,
  )
  
  val someTxt = { params: SomeTxtParams -> "${params.name} ${params.prop}" }
  //fun someTxt(name: String, prop: String) = "$name $prop"
  
  
  
  val uiText = UiText(
    value = "kjdfl",
    lang = "ru-RU",
    text = "fdsf",
  )
  val uiTemplate = UiTemplateText(
    value = "kjdfl",
    lang = "ru-RU",
    text = someTxt,
  )
  
  val templateText = uiTemplate.text(SomeTxtParams(name = "lkjkj", prop = "ljf"))
  
  
  val uiValueList = listOf(uiText, uiTemplate)
}

