package com.rrain.kupidon.service.mongo

import com.mongodb.client.model.Filters
import com.rrain.kupidon.model.db.UserM
import com.rrain.kupidon.model.db.projectionUserM
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



suspend fun findUserById(id: UUID) = collUsers
  .find(Filters.eq(UserM::id.name, id))
  .projectionUserM()
  .firstOrNull()

