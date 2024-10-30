package com.rrain.kupidon.service.lang



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

