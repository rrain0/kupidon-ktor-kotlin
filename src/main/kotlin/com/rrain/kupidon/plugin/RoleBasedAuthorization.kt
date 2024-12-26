package com.rrain.kupidon.plugin

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.util.Logger.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*



/*
● Идентификация — определить id для конкретного юзера —
  процедура, в результате выполнения которой для субъекта идентификации
  выявляется его идентификатор, однозначно определяющий этого субъекта в информационной системе.
  
● Аутентификация — проверка логина / пароля
  — HTTP Error: 401 Unauthorized, To help you out,
    it will always include a 'WWW-Authenticate' header that describes how to authenticate.
  — процедура проверки подлинности,
    например проверка подлинности пользователя путем сравнения введенного им пароля с паролем,
    сохраненным в базе данных.
  
● Авторизация — проверка ролей пользователя
  — HTTP Error: 403 Forbidden
  — предоставление определенному лицу или группе лиц прав
    на выполнение определенных действий.
*/

private fun getRoleFromCall(call: ApplicationCall): Set<Role> {
  return call.principal<JWTPrincipal>()
    ?.getListClaim(JwtService.accessTokenRolesClaimName, String::class)
    ?.map(Role::valueOf)
    ?.toSet()
    ?: emptySet()
}



class RoleBasedAuthorizationPluginConfiguration {
  var roles: Set<Role> = emptySet()
  var callToRoles: (ApplicationCall) -> Set<Role> = ::getRoleFromCall
}

// RBAC - role-based access control
val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
  name = "RbacPlugin",
  createConfiguration = ::RoleBasedAuthorizationPluginConfiguration
) {
  val logger = logger()
  val roles = pluginConfig.roles
  
  pluginConfig.apply {
    on(AuthenticationChecked) { call ->
      val tokenRoles = pluginConfig.callToRoles(call)
      
      // No need roles or has at least 1 demanded role
      val authorized = roles.isEmpty() || tokenRoles.intersect(roles).isNotEmpty()
      if (!authorized) {
        val msg = "User must have at least one of these roles: ${roles.joinToString()}"
        logger.debug("Authorization failed for ${call.request.path()}. $msg")
        call.respond(HttpStatusCode.Forbidden, object {
          val code = "LACK_OF_ROLE"
          val needAnyOfRoles = roles
          val msg = msg
        })
      }
    }
  }
}

fun Route.authorized(vararg roles: Role, build: Route.() -> Unit) {
  install(RoleBasedAuthorizationPlugin) {
    this.roles = roles.toSet()
  }
  build()
}















