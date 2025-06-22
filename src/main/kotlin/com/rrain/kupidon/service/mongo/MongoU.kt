package com.rrain.kupidon.service.mongo

import com.mongodb.MongoException
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.rrain.util.any.objectPrimaryPropsToMap
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



suspend fun <T : Any> MongoCollection<T>.findOneOrInsert(
  filter: Bson,
  toInsert: T,
) = findOneAndUpdate(
  filter,
  // !!! This gets only props from the primary constructor
  Updates.setOnInsert(Document(toInsert.objectPrimaryPropsToMap())),
  FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER),
)!!



inline fun <reified T : Any> T.toUpdatesSetAllProps(): List<Bson> = (
  this.objectPrimaryPropsToMap().map { (key, value) -> Updates.set(key, value) }
)



fun UpdatesUpdatedAt(fieldName: String, updateTime: Instant) = (
  // Без обёртки в массив (listOf) не работает.
  listOf(Updates.set(
    fieldName,
    Document($$"$cond", Document()
      .append("if", Document(
        $$"$eq", listOf($$"$$${fieldName}", updateTime)
      ))
      // Чтобы заблокировать документ для записи для других,
      // нужно 100% установить новое значение, а не такое же.
      .append("then", updateTime + 1.milliseconds)
      .append("else", updateTime)
    )
  ))
)
