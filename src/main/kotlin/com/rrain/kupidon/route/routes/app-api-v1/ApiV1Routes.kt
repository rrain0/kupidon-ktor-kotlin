package com.rrain.kupidon.route.routes.`app-api-v1`



object ApiV1Routes {
  val pwaManifest = "/manifest.json"
  
  
  val apiV1 = "/api/v1"
  
  val auth = "$apiV1/auth"
  val authLogin = "$auth/login"
  val authRefreshTokens = "$auth/refresh-tokens"
  
  val user = "$apiV1/user"
  val userCurrent = "$user/current"
  val userIdId = "$user/id/{id}"
  val userProfilePhoto = "$user/profile-photo"
  val userProfilePhotoName = "$user/profile-photo/*"
  object userProfilePhotoNameParams {
    val userId = "userId"
    val photoId = "photoId"
  }
  val userVerificationEmailInitial = "$user/verification/email/initial"
  object userVerificationEmailInitialParams {
    val verificationToken = "verification-token"
  }
  
  val users = "$apiV1/users"
  val usersMutualLiked = "$users/mutual-liked"
  
  val userToUser = "$apiV1/user-to-user"
  val userToUserLike = "$userToUser/like"
  
  val chatMessage = "$apiV1/chat-message"
  
  val chatMessages = "$apiV1/chat-messages"
}


