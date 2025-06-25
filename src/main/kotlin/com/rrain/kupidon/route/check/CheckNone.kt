package com.rrain.kupidon.route.check

import com.mongodb.client.model.Filters



// Везде id типа UUID, сравнение со строкой всегда будет false.
fun filterNone() = Filters.eq("id", "")