package com.rrain.kupidon.util

import com.rrain.kupidon.entity.app.Gender


fun main(){
  println(Gender.valueOf("MALE"))
  
  // Exception in thread "main" java.lang.IllegalArgumentException:
  // No enum constant com.rrain.kupidon.entity.app.Gender.123
  if (false) println(Gender.valueOf("123"))
}