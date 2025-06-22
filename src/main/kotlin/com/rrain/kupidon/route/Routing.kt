package com.rrain.kupidon.route

import com.rrain.kupidon.route.routes.`app-api-v1`.auth.addAuthOtherRoutes
import com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`.addChatMessageToChatIdIdPostRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.`chat-message`.addChatMessageToUserIdIdPostRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.`chat-messages`.addChatMessagesRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.chats.addChatsRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.users.addUsersMutuallyLikedRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.`user-to-user`.addUserToUserLikeRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserCreateRoute
import com.rrain.kupidon.route.routes.main.addMainRoutes
import com.rrain.kupidon.route.routes.`app-pwa-manifest`.addPwaManifestRoute
import com.rrain.kupidon.route.routes.test.*
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserCurrentRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserEmailInitialVerifyRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserIdIdRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserUpdateRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserProfilePhotoPostRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.user.addUserProfilePhotoGetRoute
import com.rrain.kupidon.route.routes.`app-api-v1`.users.addUsersRoute
import com.rrain.kupidon.route.routes.app.api.v1.auth.addAuthLoginRoute
import com.rrain.kupidon.route.routes.app.api.v1.auth.addAuthRefreshTokensRoute
import io.ktor.server.application.*
import io.ktor.server.routing.IgnoreTrailingSlash



fun Application.configureRouting() {
  
  install(IgnoreTrailingSlash)
  
  
  addMainRoutes()
  
  addAuthorizationTestRoutes()
  addHttpTestRoutes()
  addImgTestRoutes()
  addJsonSerializationTestRoutes()
  addMongoTestRoutes()
  addSendEmailTestRoutes()
  
  
  addPwaManifestRoute()
  
  addAuthLoginRoute()
  addAuthRefreshTokensRoute()
  addAuthOtherRoutes()
  
  addUserCurrentRoute()
  addUserCreateRoute()
  addUserUpdateRoute()
  addUserIdIdRoute()
  addUserProfilePhotoPostRoute()
  addUserProfilePhotoGetRoute()
  addUserEmailInitialVerifyRoute()
  
  addUserToUserLikeRoute()
  
  addUsersRoute()
  addUsersMutuallyLikedRoute()
  
  addChatsRoute()
  
  addChatMessageToUserIdIdPostRoute()
  addChatMessageToChatIdIdPostRoute()
  
  addChatMessagesRoute()
  
}


