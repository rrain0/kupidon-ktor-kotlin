package com.rrain.kupidon.plugin

import com.rrain.kupidon.model.Role
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.util.cast
import com.rrain.kupidon.util.logger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
//import mu.KotlinLogging



/*
● Идентификация — определить id для конкретного юзера —
  процедура, в результате выполнения которой для субъекта идентификации
  выявляется его идентификатор, однозначно определяющий этого субъекта в информационной системе.
  
● Аутентификация — проверка логина/пароля
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


fun Application.configureRoleBasedAuthorization(){
  
  install(RoleBasedAuthorization) {
    getRoles = {
      it?.let {
         it.cast<JWTPrincipal>()
          .getListClaim(JwtService.accessTokenRolesClaimName, String::class)
          .map(Role::valueOf)
          .toSet()
      } ?: emptySet()
    }
  }
  
}





class RoleBasedAuthorization(config: Configuration) {
  
  private val logger = logger()
  
  private val getRoles = config.getRoles
  
  class Configuration {
    // configure role extraction for particular principal coming from Authorization plugin
    lateinit var getRoles: (Principal?) -> Set<Role>
  }
  
  
  fun interceptPipeline(
    pipeline: ApplicationCallPipeline,
    anyOfRoles: Set<Role> = emptySet(),
  ) {
    val authenticatePhase: PipelinePhase = PipelinePhase("Authenticate")
    // By default, we have 5 phases: Setup, Monitoring, Plugins (Features), Call, Fallback.
    // Adding Authenticate phase into our pipeline.
    pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, authenticatePhase)
    // Adding Authorization phase after Authentication phase in our pipeline
    pipeline.insertPhaseAfter(authenticatePhase, RoleBasedAuthorizationPhase)
    
    pipeline.intercept(RoleBasedAuthorizationPhase) {
      val principal = call.authentication.principal<Principal>()!!
      val roles = getRoles(principal)
      
      if (anyOfRoles.isNotEmpty() && (anyOfRoles - roles).size == anyOfRoles.size) {
        val msg = "User must have at least one of these roles: ${anyOfRoles.joinToString()}"
        logger.debug("Authorization failed for ${call.request.path()}. $msg")
        call.respond(HttpStatusCode.Forbidden, object {
          val code = "LACK_OF_ROLE"
          val needAnyOfRoles = anyOfRoles
          val msg = msg
        })
      }
    }
  }
  
  companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, RoleBasedAuthorization> {
    override val key = AttributeKey<RoleBasedAuthorization>("RoleBasedAuthorization")
    
    val RoleBasedAuthorizationPhase = PipelinePhase("Authorization")
    
    override fun install(
      pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit
    ): RoleBasedAuthorization {
      val configuration = Configuration().apply(configure)
      val plugin = RoleBasedAuthorization(configuration)
      // Intercept a pipeline ...
      /*
      pipeline.intercept(ApplicationCallPipeline.Plugins) {
        call.response.header("X-Custom-Header", "Hello, world!")
      }
       */
      
      return plugin
    }
  }
  
}


class AuthorizedRouteSelector(private val description: String) : RouteSelector() {
  override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
  override fun toString(): String = "(authorize ${description})"
}


fun Route.withAnyRole(vararg roles: Role, build: Route.() -> Unit) =
  authorizedRoute(anyOfRoles = roles.toSet(), build = build)


private fun Route.authorizedRoute(
  anyOfRoles: Set<Role>,
  build: Route.() -> Unit
): Route {
  val description = "(anyOf(${anyOfRoles.joinToString(",")}))"
  val authorizedRouteSelector = AuthorizedRouteSelector(description)
  val authorizedRoute = createChild(authorizedRouteSelector)
  
  val plugin = application.plugin(RoleBasedAuthorization)
  plugin.interceptPipeline(authorizedRoute, anyOfRoles)
  
  authorizedRoute.build()
  return authorizedRoute
}