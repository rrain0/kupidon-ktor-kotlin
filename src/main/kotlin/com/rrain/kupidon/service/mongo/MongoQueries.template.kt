package com.rrain.kupidon.service.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.rrain.util.uuid.randomUUID
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID
import kotlin.random.Random



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
  val idToFind = randomUUID()
  val item = collTestItems()
    .find(Filters.eq(TestItem::id.name, idToFind))
    .firstOrNull()
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
  var item = TestItem()
  mongoUniqueViolationRetry(
    {
      item = collTestItems().findOneOrInsert(
        Filters.eq(TestItem::id.name, item.id),
        item,
      )
    },
    { item.id = randomUUID() },
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
      Updates.combine(itemUpdate.toUpdatesSetAllProps()),
      FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
    )
  return item
}


// Find one and update.
// Find one, update it, return it. If not found, then return null.
// 1️⃣ Single document operation.
// ⚛️ Atomic operation.
// ⏳ Awaits transactions.
private suspend fun MongoClient.findOneThenProcessThenUpdate(): TestItem? {
  val itemUpdate = TestItem()
  val item = collTestItems()
    .findOneAndUpdate(
      Filters.eq(TestItem::id.name, itemUpdate.id),
      Updates.combine(itemUpdate.toUpdatesSetAllProps()),
      FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER),
    )
  return item
}