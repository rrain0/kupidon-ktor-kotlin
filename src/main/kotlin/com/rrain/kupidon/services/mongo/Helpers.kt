package com.rrain.kupidon.services.mongo

import com.mongodb.client.model.Filters
import com.rrain.kupidon.models.db.UserM
import com.rrain.kupidon.models.db.projectionUserM
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID



suspend fun findUserById(id: UUID) = collUsers
  .find(Filters.eq(UserM::id.name, id))
  .projectionUserM()
  .firstOrNull()

suspend fun findUserByEmail(email: String) = collUsers
  .find(Filters.eq(UserM::email.name, email))
  .projectionUserM()
  .firstOrNull()

