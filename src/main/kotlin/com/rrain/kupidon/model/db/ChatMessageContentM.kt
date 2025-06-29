package com.rrain.kupidon.model.db



data class ChatMessageContentM(
  var text: String,
) {
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "text" to text,
    )
  }
}