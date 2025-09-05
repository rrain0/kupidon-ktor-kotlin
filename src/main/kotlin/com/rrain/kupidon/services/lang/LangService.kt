package com.rrain.kupidon.services.lang



enum class Lang(val value: String) {
  enUS("en-US"),
  ruRU("ru-RU"),
  ;
  
  companion object {
    val Default = enUS
    
    fun getOrDefault(value: String?): Lang {
      return Lang.entries.find { it.value == value } ?: Default
    }
  }
  
}


