package com.rrain.kupidon.service.db.ARCHIVE.postgres

import io.r2dbc.spi.Connection


inline fun <T>Connection.use(block: (connection: Connection)->T): T = try {
  this.beginTransaction()
  val t = block(this)
  this.commitTransaction()
  t
} finally {
  this.close()
}