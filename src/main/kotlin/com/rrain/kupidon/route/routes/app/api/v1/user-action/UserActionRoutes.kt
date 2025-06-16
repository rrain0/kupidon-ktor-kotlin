package com.rrain.kupidon.route.routes.app.api.v1.`user-action`

import com.rrain.kupidon.route.routes.app.api.v1.ApiV1Routes
import com.rrain.kupidon.route.routes.app.api.v1.`user-action`.routes.configureUserActionRouteLike
import io.ktor.server.application.*




object UserActionRoutes {
  const val base = "${ApiV1Routes.base}/user-to-user-like"
  
  const val list = "${base}/list"
  
  const val listAll = "${list}/all"
}



fun Application.configureUserActionRoutes() {
  
  configureUserActionRouteLike()
  
}