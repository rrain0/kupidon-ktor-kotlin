package com.rrain.kupidon.route.routes.`app-api-v1`.user

import com.mongodb.client.model.Filters
import com.rrain.kupidon.model.Gender
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.service.PwdHashService
import com.rrain.kupidon.service.lang.`ui-values`.AppUiText
import com.rrain.kupidon.service.lang.`ui-values`.EmailInitialVerificationUiText
import com.rrain.kupidon.route.`response-errors`.respondBadRequest
import com.rrain.kupidon.route.`response-errors`.respondInvalidBody
import com.rrain.kupidon.service.mongo.model.UserM
import com.rrain.kupidon.service.lang.Lang
import com.rrain.kupidon.`mini-libs`.`ui-text`.pickUiValue
import com.rrain.kupidon.route.routes.`app-api-v1`.ApiV1Routes
import com.rrain.kupidon.service.mongo.collUsers
import com.rrain.kupidon.service.mongo.model.UserDataType
import com.rrain.kupidon.service.mongo.model.projectionUserM
import com.rrain.kupidon.service.mongo.useSingleDocTx
import com.rrain.`util-ktor`.call.host
import com.rrain.`util-ktor`.call.port
import com.rrain.`util-ktor`.call.queryParams
import com.rrain.util.`date-time`.now
import com.rrain.util.`date-time`.toZonedInstant
import com.rrain.util.validation.emailPattern
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.yearsUntil
import java.util.*




fun Application.addUserCreateRoute() {
  routing {
    
    data class UserCreateBodyIn(
      val email: String,
      val pwd: String,
      val name: String,
      val gender: Gender,
      val birthDate: LocalDate,
    )
    
    post(ApiV1Routes.user) {
      val userToCreate =
        try { call.receive<UserCreateBodyIn>() }
        catch (ex: Exception) { return@post call.respondInvalidBody() }
      val langs = call.queryParams.getAll("lang")!!.map { Lang.getOrDefault(it) }
      val timeZone = call.queryParams["timeZone"].let { TimeZone.of(it ?: "UTC+0") }
      
      
      if (!userToCreate.email.matches(emailPattern)) {
        return@post call.respondInvalidBody(
          "Invalid email format"
        )
      }
      if (userToCreate.email.length > 100) {
        return@post call.respondInvalidBody(
          "Email max length is 100 chars"
        )
      }
      
      if (userToCreate.pwd.length < 6) {
        return@post call.respondInvalidBody(
          "Password must be at least 6 chars length"
        )
      }
      if (userToCreate.pwd.length > 200) {
        return@post call.respondInvalidBody(
          "Password max length is 200 chars"
        )
      }
      
      if (userToCreate.name.isEmpty()) {
        return@post call.respondInvalidBody(
          "Name must not be empty"
        )
      }
      if (userToCreate.name.length > 100) {
        return@post call.respondInvalidBody(
          "Name max length is 100"
        )
      }
      
      
      val now = now()
      if (userToCreate.birthDate.toZonedInstant(timeZone)
        .yearsUntil(now, timeZone) < 18
      ) {
        return@post call.respondInvalidBody("You must be 18+ years old")
      }
      
      
      val nowZoned = now()
      val tryUser = UserM(
        id = UUID.randomUUID(),
        roles = setOf(),
        email = userToCreate.email,
        pwd = userToCreate.pwd.let(PwdHashService::generateHash),
        createdAt = nowZoned,
        updatedAt = nowZoned,
        name = userToCreate.name,
        birthDate = userToCreate.birthDate,
        gender = userToCreate.gender,
        aboutMe = "",
        photos = listOf(),
      )
      
      
      val user = useSingleDocTx { session ->
        val nUserId = UserM::id.name
        val nUserEmail = UserM::email.name
        
        val userByEmail = collUsers
          .find(session, Filters.eq(nUserEmail, tryUser.email))
          .projectionUserM()
          .firstOrNull()
        
        if (userByEmail != null) return@post call.respondBadRequest(
          code = "DUPLICATE_EMAIL",
          msg = "User with such email already exists",
        )
        
        collUsers
          .insertOne(session, tryUser)
        
        collUsers
          .find(session, Filters.eq(nUserId, tryUser.id))
          .projectionUserM()
          .first()
      }
      
      
      val id = user.id
      val verificationToken = JwtService.generateVerificationAccessToken(
        id.toString(), user.email
      )
      
      
      val appName = AppUiText.appName.pickUiValue(langs).value
      val emailTitle = EmailInitialVerificationUiText.emailTitle.pickUiValue(langs).value
      val emailContent = EmailInitialVerificationUiText.emailContent.pickUiValue(langs).value(
        EmailInitialVerificationUiText.EmailContentParams(
          userName = user.name,
          verificationUrl = call.request.origin.run {
            val url = "$scheme://$serverHost:$serverPort${ApiV1Routes.userVerificationEmailInitial}"
            val query = "${ApiV1Routes.userVerificationEmailInitialParams.verificationToken}=$verificationToken"
            url + query
          },
        )
      )
      
      // todo email - Ссылка действительна 1 сутки, иначе войдите в приложение и запросите новую или смените почту.
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
          // todo email - validate if email was sent successfully
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
      call.respond(mapOf(
        "accessToken" to accessToken,
        "user" to user.toApi(UserDataType.Current, call.host, call.port, timeZone),
      ))
    }
    
    
    
    
  }
}