package com.rrain.kupidon.service.mongo

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.TransactionOptions
import com.mongodb.WriteConcern
import com.mongodb.kotlin.client.coroutine.ClientSession




// READ PREFERENCE
// https://www.mongodb.com/docs/manual/core/read-preference/#std-label-replica-set-read-preference
// Read preference describes how MongoDB clients route read operations to the members of a replica set.
// Useful for balancing consistency vs. latency.
// `primary`✅✅: Reads only from the primary node (default, ensures strong consistency).
// `primaryPreferred`: Prefers the primary but falls back to a secondary if the primary is unavailable.
// `secondary`: Reads from secondary nodes (may return slightly stale data but reduces load on the primary).
// `secondaryPreferred`: Prefers secondaries but falls back to the primary if no secondary is available.
// `nearest`: Reads from the node with the lowest latency, regardless of primary or secondary.

// READ CONCERN
// https://www.mongodb.com/docs/manual/reference/read-concern/
// The readConcern option allows you to control the consistency
// and isolation properties of the data read from replica sets and replica set shards.
// Ensures the data read is consistent with the transaction’s requirements.
// `local`✅: Sufficient for SINGLE-DOCUMENT operations.
// Returns the most recent data available on the node (default, may not reflect writes acknowledged by a majority).
// `majority`✅➕: Returns data that has been acknowledged by a majority of replica set members
// (ensures durability but may be slower).
// `linearizable`✅✅➕: Ensures reads reflect all successful writes that completed before the read
// (strong consistency, only for primary reads).
// `snapshot`✅✅: Reads from a consistent snapshot of data at the transaction’s start
// (ideal for multi-document transactions, MongoDB 4.0+).
// `available`: Reads the most recent data, even if uncommitted (not typically used in transactions).

// WRITE CONCERN
// https://www.mongodb.com/docs/manual/reference/write-concern/
// Write concern describes the level of acknowledgment requested from MongoDB for write operations
// to a standalone mongod or to Replica sets or to sharded clusters.
// In sharded clusters, mongos instances will pass the write concern on to the shards.
// Ensures writes are durable and consistent across the replica set.
// `w: 1`: Acknowledges the write on the primary node (default, fast but less durable).
// `w: "majority"`🔶: For CRITICAL transactions. Acknowledges the write after a majority of replica set members replicate it (slower but durable).
// `w: <number>`: Acknowledges after `<number>` nodes replicate the write.
// `journal: true`🔶: Ensures writes are written to the journal on disk before acknowledgment (increases durability).
// `wTimeout: <ms>`: Sets a timeout (in milliseconds) for the write operation to complete, or it fails.

// NOTES
// ℹ️ Попытка установить то же самое значение полю НЕ вызывает блокировку документа.
// ℹ️ Любое обновление любого поля документа новым значением, отличным от предыдущего,
// вызывает блок на всём документе.



val singleDocTxOpts = TransactionOptions.builder()
  .readPreference(ReadPreference.primary())
  .readConcern(ReadConcern.LOCAL)
  .writeConcern(WriteConcern.MAJORITY.withJournal(true))
  .build()

val readManyDocsTxOpts = TransactionOptions.builder()
  .readConcern(ReadConcern.SNAPSHOT)
  .build()

class TxAbortedException : IllegalStateException()

suspend inline fun <T> useTx(
  transactionOpts: TransactionOptions,
  block: (session: ClientSession, abort: suspend () -> Unit) -> T,
): T {
  return mongo.startSession().use { session ->
    var aborted = false
    val abort = suspend { session.abortTransaction(); aborted = true }
    
    session.startTransaction(transactionOpts)
    val result = block(session, abort)
    if (aborted) throw TxAbortedException()
    session.commitTransaction()
    
    result
  }
}



typealias TxBlock<T> = (session: ClientSession) -> T
typealias TxBlockWithAbort<T> = (session: ClientSession, abort: suspend () -> Unit) -> T



suspend inline fun <T> useSingleDocTx(block: TxBlock<T>): T = (
  useSingleDocTx { session, abort -> block(session) }
)
suspend inline fun <T> useSingleDocTx(block: TxBlockWithAbort<T>): T = (
  useTx(singleDocTxOpts, block)
)

