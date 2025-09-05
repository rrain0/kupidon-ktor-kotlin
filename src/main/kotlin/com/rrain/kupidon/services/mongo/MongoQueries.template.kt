package com.rrain.kupidon.services.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.rrain.kupidon.models.db.ChatMessageM
import com.rrain.utils.bson.toBJson
import com.rrain.utils.bson.toDoc
import com.rrain.utils.base.`date-time`.now
import com.rrain.utils.base.`delegated-prop`.getIt
import com.rrain.utils.base.uuid.randomUuid
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.random.Random


/*
Operators:

🔶 $eq
Checks if stored-value == value or stored-array-value contains value.
{ fieldName: { $eq: value } }
Filters.eq(fieldName, value)

🔹Operators in expression { $expr: ... }
🔹Can get field values by $<filedName>.
🔹Can use local variables by $$<localVariable> in aggregation lookup.
Operator transformations inside $expr (if operator is supported):
🔹{ <fieldName>: { $<operator>: <value> } } => { $<operator>: [$<fieldName>, <value>] }
🔶🔹$eq
{ fieldName: { $eq: value } } => { $eq: [fieldName, value] }
❌🔹$all - not supported
🔶🔹$in
{ fieldName: { $in: arrayValue } } => { $in: [fieldName, arrayValue] }
 */



private data class TestItem(
  var id: UUID = UUID.randomUUID(),
  var createdAt: Instant = Clock.System.now(),
  var updatedAt: Instant = Clock.System.now(),
  var number: Int = Random.nextInt(),
  var string: String = "",
  var integer: Int = Random.nextInt(),
)
private fun MongoClient.collTestItems() = getDatabase("testItemsDb").coll<TestItem>("testItems")


// Find one.
// 1️⃣ Single document operation.
// ⚛️ Atomic operation.
// ⚡️ Immediate. Impossible to block by writings or transactions.
private suspend fun MongoClient.findOne(): TestItem? {
  val idToFind = randomUuid()
  val item = collTestItems()
    .find(Filters.eq(TestItem::id.name, idToFind))
    .firstOrNull()
  return item
}


// Insert one (and get inserted document).
// 1️⃣ Single document operation.
// ⚛️ Atomic operation.
// ⚡️ Immediate. Impossible to block by writings or transactions.
private suspend fun MongoClient.insertOneAndGetIt(): TestItem {
  var item = TestItem()
  mongoUniqueViolationRetry(
    {
      item = collTestItems().findOneOrInsert(
        // Condition that always matches none to guarantee insertion.
        // Because UUID id is not string and not empty string.
        Filters.eq(ChatMessageM::id.name, ""),
        item,
      )
    },
    { item.id = UUID.randomUUID() },
  )
  return item
}


// Find one or insert.
// Find one and return it. If not found, then insert the provided one and return it.
// 1️⃣ Single document operation.
// ⚛️ Atomic operation.
// ⚡️ Immediate. Impossible to block by writings or transactions.
// 🔄 If there is a unique index for data to insert and data already in db,
// then this handles exception and retires with your new provided data.
private suspend fun MongoClient.findOneOrInsert(): TestItem {
  val i by lazy { 1 }
  val ii by getIt { 1 }
  var item = TestItem()
  mongoUniqueViolationRetry(
    {
      item = collTestItems().findOneOrInsert(
        Filters.eq(TestItem::id.name, item.id),
        item,
      )
    },
    { item.id = randomUuid() },
  )
  return item
}


// Find one and update.
// Find one, update it, return it. If not found, then return null.
// 1️⃣ Single document operation.
// ⚛️ Atomic operation.
// ⏳ Awaits transactions.
private suspend fun MongoClient.findOneAndUpdate(): TestItem? {
  val itemUpdate = TestItem()
  val item = collTestItems()
    .findOneAndUpdate(
      Filters.eq(TestItem::id.name, itemUpdate.id),
      $$"{ $set: $${itemUpdate.toBJson()} }".toDoc(),
      FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
    )
  return item
}


// Find one and update.
// Find one, update it, return it. If not found, then return null.
// 1️⃣ Single document operation.
// ⚛️ Atomic transaction.
// ⏳ Awaits transactions.
private suspend fun MongoClient.findOneThenProcessThenUpdate(): TestItem? {
  val itemUpdate = TestItem()
  val updatedItem = useSingleDocTx { session, abort ->
    val now = now()
    // Lock item by changing updatedAt
    var item = collTestItems()
      .findOneAndUpdate(
        session,
        Filters.eq(TestItem::id.name, itemUpdate.id),
        listOf(UpdatesUpdatedAt(TestItem::updatedAt.name, now)),
        FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
      )
    
    // Do some checks and updates
    if (item == null) {
      abort()
      throw RuntimeException("Item not found")
    }
    if (item.integer < 200) item.integer++
    
    item = collTestItems()
      .findOneAndUpdate(
        session,
        Filters.eq(TestItem::id.name, itemUpdate.id),
        $$"{ $set: $${item.toBJson()} }".toDoc(),
        FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
      )!!
    
    item
  }
  return updatedItem
}