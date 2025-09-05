package com.rrain.kupidon.models.db



data class ChatMessageContentM(
  var text: String,
) {
  fun toApi(): MutableMap<String, Any?> {
    return mutableMapOf(
      "text" to text,
    )
  }
}