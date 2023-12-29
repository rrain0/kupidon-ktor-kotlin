package com.rrain.kupidon.route.route.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.entity.app.Gender
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashing
import com.rrain.kupidon.service.lang.AppLang
import com.rrain.kupidon.service.lang.prepareUiValues
import com.rrain.kupidon.service.lang.`ui-value`.AppUiText
import com.rrain.kupidon.service.lang.`ui-value`.EmailInitialVerificationUiText
import com.rrain.kupidon.route.util.respondBadRequest
import com.rrain.kupidon.route.util.respondInvalidBody
import com.rrain.kupidon.service.db.mongo.MongoDbService
import com.rrain.kupidon.service.db.mongo.coll
import com.rrain.kupidon.service.db.mongo.db
import com.rrain.kupidon.service.db.mongo.entity.UserMongo
import com.rrain.kupidon.service.db.mongo.entity.UserProfilePhotoMongo
import com.rrain.kupidon.service.db.mongo.useTransaction
import com.rrain.kupidon.util.emailPattern
import com.rrain.kupidon.util.zonedNow
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*




fun Application.configureUserRouteCreate() {
  
  fun mongo() = MongoDbService.client
  
  
  
  
  routing {
    
    
    
    data class UserCreateReq(
      val email: String,
      val pwd: String,
      val name: String,
      val gender: Gender,
      val birthDate: ZonedDateTime,
    )
    post(UserRoutes.create) {
      val userToCreate = try {
        call.receive<UserCreateReq>()
      }
      catch (ex: Exception){
        return@post call.respondInvalidBody()
      }
      
      
      if (!userToCreate.email.matches(emailPattern)){
        return@post call.respondInvalidBody(
          "Invalid email format"
        )
      }
      if (userToCreate.email.length>100)
        return@post call.respondInvalidBody(
          "Email max length is 100 chars"
        )
      
      if (userToCreate.pwd.length<6){
        return@post call.respondInvalidBody(
          "Password must be at least 6 chars length"
        )
      }
      if (userToCreate.pwd.length>200)
        return@post call.respondInvalidBody(
          "Password max length is 200 chars"
        )
      
      if (userToCreate.name.isEmpty()){
        return@post call.respondInvalidBody(
          "Name must not be empty"
        )
      }
      if (userToCreate.name.length>100){
        return@post call.respondInvalidBody(
          "Name max length is 100"
        )
      }
      
      val nowWithUserZone = zonedNow()
        .withZoneSameInstant(userToCreate.birthDate.zone)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
      if (ChronoUnit.YEARS.between(userToCreate.birthDate, nowWithUserZone)<18){
        return@post call.respondInvalidBody(
          "You must be at least 18 years old"
        )
      }
      
      
      val now = zonedNow()
      val tryUser = UserMongo(
        id = UUID.randomUUID(),
        roles = setOf(),
        email = userToCreate.email,
        pwd = userToCreate.pwd.let(PwdHashing::generateHash),
        created = now,
        updated = now,
        name = userToCreate.name,
        birthDate = userToCreate.birthDate.toLocalDate(),
        gender = userToCreate.gender,
        aboutMe = "",
        photos = (0..5).map { null },
        transactions = null,
      )
      
      
      val m = mongo()
      val user = m.useTransaction { session ->
        val nUserId = UserMongo::id.name
        val nUserEmail = UserMongo::email.name
        val nUserPhotos = UserMongo::photos.name
        val nPhotoBinData = UserProfilePhotoMongo::binData.name
        
        val userByEmail = m.db.coll<UserMongo>("users")
          .find(session, Filters.eq(nUserEmail, tryUser.email))
          .projection(Document("$nUserPhotos.$nPhotoBinData", false))
          .firstOrNull()
        
        if (userByEmail!=null) return@post call.respondBadRequest(
          code = "DUPLICATE_EMAIL",
          msg = "User with such email already exists",
        )
        
        m.db.coll<UserMongo>("users")
          .insertOne(session, tryUser)
        
        m.db.coll<UserMongo>("users")
          .find(session, Filters.eq(nUserId, tryUser.id))
          .projection(Document("$nUserPhotos.$nPhotoBinData", false))
          .first()
      }
      
      
      val id = user.id
      val verificationToken = JwtService
        .generateVerificationAccessToken(id.toString(), user.email)
      
      val lang = AppLang.getByValueOrDefault(call.parameters["lang"])
      val langs = listOf(lang.value)
      
      val appName = AppUiText.appName.prepareUiValues(langs)[0].text
      val emailTitle = EmailInitialVerificationUiText.emailTitle.prepareUiValues(langs)[0].text
      val emailContent = EmailInitialVerificationUiText.emailContent.prepareUiValues(langs)[0].text(
        EmailInitialVerificationUiText.EmailContentParams(
          userName = user.name,
          verificationUrl = call.request.origin.run {
            val url = "$scheme://$serverHost:$serverPort${UserRoutes.emailInitialVerification}"
            val query = "${UserRoutes.verifyTokenParamName}=$verificationToken"
            url+query
          },
        )
      )
      
      // TODO Ссылка действительна 1 сутки, иначе войдите в приложение и запросите новую или смените почту.
      /*launch {
        try {
          EmailService.sendHtmlEmail(
            EmailMessage(
              fromName = appName,
              to = user.email,
              title = emailTitle,
              content = emailContent,
            )
          )
        } catch (ex: EmailException) {
          // Ошибка отправки
          // TODO validate if email was sent successfully
          ex.printStackTrace()
        }
      }*/
      
      val roles = user.roles
      
      val domain = call.request.origin.serverHost
      
      val accessToken = JwtService.generateAccessToken(id.toString(), roles)
      val refreshToken = JwtService.generateRefreshToken(id.toString())
      
      // сделать позже save refresh token & device info to db as opened session
      
      call.response.cookies.append(
        JwtService.generateRefreshTokenCookie(refreshToken,domain)
      )
      call.respond(object {
        val accessToken = accessToken
        val user = user.convertToSend(call.request)
      })
    }
    
    
    
    
  }
}