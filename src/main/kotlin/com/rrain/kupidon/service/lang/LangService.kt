package com.rrain.kupidon.service.lang





enum class AppLangEnum(
  val value: String,
) {
  eng("en-US"),
  rus("ru-RU"),
  ;
  
  companion object {
    fun getByValueOrDefault(value: String?): AppLangEnum {
      return AppLangEnum.entries.find { it.value==value } ?: DefaultAppLang
    }
  }
}
val DefaultAppLang = AppLangEnum.eng


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
      else 0
    }
    .distinctBy { it.value }
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

data class UiTemplate<T : Function<String>>(
  override val value: String,
  override val lang: String,
  override val text: T,
) : UiValue()



val UndefinedUiText = UiText("undefined", DefaultAppLang.value, "undefined")



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
  val uiTemplate = UiTemplate(
    value = "kjdfl",
    lang = "ru-RU",
    text = someTxt,
  )
  
  val templateText = uiTemplate.text(SomeTxtParams(name = "lkjkj", prop = "ljf"))
  
  
  val uiValueList = listOf(uiText, uiTemplate)
}

