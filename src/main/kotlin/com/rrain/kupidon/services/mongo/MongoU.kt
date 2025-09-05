package com.rrain.kupidon.services.mongo

import com.mongodb.MongoException
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.rrain.utils.bson.toBJson
import com.rrain.utils.bson.toDoc
import kotlinx.datetime.Instant
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.time.Duration.Companion.milliseconds



suspend fun mongoUniqueViolationRetry(
  block: suspend (attemptN: Int) -> Unit,
  onError: suspend () -> Unit,
) {
  val attemptsCnt = 100
  for (i in 1..attemptsCnt) {
    try {
      block(i)
      break
    }
    catch (ex: MongoException) {
      if (i == attemptsCnt) throw ex
      // Ошибка нарушения уникальности индекса
      else if (ex.code == 11000) onError()
      else throw ex
    }
  }
}



// !!! filter { arrayField: { $all: [1, 2] } }
// with { $setOnInsert: { arrayField: [1, 2] } }
// produces error. I suppose this is a bug.
suspend fun <T : Any> MongoCollection<T>.findOneOrInsert(
  filter: Bson,
  toInsert: T,
) = findOneAndUpdate(
  filter,
  $$"{ $setOnInsert: $${toInsert.toBJson()} }".toDoc(),
  FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER),
)!!



// Этот апдейт должен быть частью aggregation pipeline, то есть лежать в массиве (listOf).
fun UpdatesUpdatedAt(fieldName: String, updateTime: Instant) = (
  Updates.set(
    fieldName,
    Document($$"$cond", Document()
      .append("if", Document($$"$eq", listOf("$${fieldName}", updateTime)))
      // Чтобы заблокировать документ для записи для других,
      // нужно 100% установить новое значение, а не такое же.
      .append("then", updateTime + 1.milliseconds)
      .append("else", updateTime)
    )
  )
)
