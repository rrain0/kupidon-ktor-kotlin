package com.rrain.kupidon.plugin

import com.rrain.kupidon.model.Permission
import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.AccessToken
import com.rrain.kupidon.service.JwtService
import com.rrain.util.logger.logger
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
  
● Авторизация — проверка ролей и разрешений пользователя
  — HTTP Error: 403 Forbidden
  — предоставление определенному лицу или группе лиц прав на выполнение определенных действий.
*/

/*
private fun getRoleFromCall(call: ApplicationCall): Set<Role> {
  return call.principal<JWTPrincipal>()
    ?.getListClaim("roles", String::class)
    ?.map(Role::valueOf)
    ?.toSet()
    ?: emptySet()
}
*/

private fun getRoleFromCall(call: ApplicationCall): Set<Role> {
  return call.principal<AccessToken>()?.roles ?: emptySet()
}



class RoleBasedAuthorizationPluginConfiguration {
  var permissions: Set<Permission> = emptySet()
  var callToRoles: (ApplicationCall) -> Set<Role> = ::getRoleFromCall
}

// RBAC - role-based access control
val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
  name = "RbacPlugin",
  createConfiguration = ::RoleBasedAuthorizationPluginConfiguration
) {
  val logger = logger()
  
  pluginConfig.apply {
    on(AuthenticationChecked) { call ->
      val endpointPermissions = permissions
      val tokenRoles = callToRoles(call)
      val tokenPermissions = tokenRoles.flatMap { it.permissions }.toSet()
      
      val authorized = tokenPermissions.containsAll(endpointPermissions)
      
      if (!authorized) {
        val msg = "User must have all these permissions: ${endpointPermissions.joinToString()}"
        logger.debug("Authorization failed for ${call.request.path()}. $msg")
        call.respond(HttpStatusCode.Forbidden, mapOf(
          "code" to "LACK_OF_PERMISSION",
          "requiredPermissions" to endpointPermissions,
          "msg" to msg,
        ))
      }
    }
  }
}

fun Route.authorize(vararg permissions: Permission, build: Route.() -> Unit) {
  install(RoleBasedAuthorizationPlugin) {
    this.permissions = permissions.toSet()
  }
  build()
}















