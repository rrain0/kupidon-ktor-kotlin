package com.rrain.kupidon.routes.`check-data`

import com.mongodb.client.model.Filters



// Везде id типа UUID, сравнение со строкой всегда будет false.
fun filterNone() = Filters.eq("id", "")