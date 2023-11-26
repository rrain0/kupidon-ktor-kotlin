package com.rrain.kupidon._old.v01.postgres.service.db

import com.rrain.kupidon.entity.app.Role
import com.rrain.kupidon.service.JwtService
import com.rrain.kupidon.util.cast
import com.rrain.kupidon.util.logger
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
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



class AuthorizationException(override val message: String) : RuntimeException(message)

class RoleBasedAuthorization(config: Configuration) {
  
  //private val logger = KotlinLogging.logger { }
  private val logger = logger()
  
  private val getRoles = config.getRoles
  //private val roles = config.roles
  
  class Configuration {
    // configure for particular principal coming from Authorization plugin
    lateinit var getRoles: (Principal?) -> Set<Role>
  }
  /*class Configuration {
    var roles: Set<Role> = emptySet()
  }*/
  
  fun interceptPipeline(
    pipeline: ApplicationCallPipeline,
    any: Set<Role>? = null,
    all: Set<Role>? = null,
    none: Set<Role>? = null
  ) {
    val AuthenticatePhase: PipelinePhase = PipelinePhase("Authenticate")
    
    pipeline.insertPhaseAfter(ApplicationCallPipeline.Plugins, AuthenticatePhase)
    pipeline.insertPhaseAfter(AuthenticatePhase, RoleBasedAuthorizationPhase)
    
    pipeline.intercept(RoleBasedAuthorizationPhase) {
      val principal =
        call.authentication.principal<Principal>() ?: throw AuthorizationException("Missing principal")
      val roles = getRoles(principal)
      val denyReasons = mutableListOf<String>()
      all?.let {
        val missing = all - roles
        if (missing.isNotEmpty()) {
          denyReasons += "Principal ${principal} lacks required role(s) ${missing.joinToString(" and ")}"
        }
      }
      any?.let {
        if (any.none { it in roles }) {
          denyReasons += "Principal ${principal} has none of the sufficient role(s) ${
            any.joinToString(
              " or "
            )
          }"
        }
      }
      none?.let {
        if (none.any { it in roles }) {
          denyReasons += "Principal ${principal} has forbidden role(s) ${
            (none.intersect(roles)).joinToString(
              " and "
            )
          }"
        }
      }
      if (denyReasons.isNotEmpty()) {
        val message = denyReasons.joinToString(". ")
        logger.warn("Authorization failed for ${call.request.path()}. ${message}")
        throw AuthorizationException(message)
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

fun Route.withRole(role: Role, build: Route.() -> Unit) = authorizedRoute(all = setOf(role), build = build)

fun Route.withAllRoles(vararg roles: Role, build: Route.() -> Unit) =
  authorizedRoute(all = roles.toSet(), build = build)

fun Route.withAnyRole(vararg roles: Role, build: Route.() -> Unit) = authorizedRoute(any = roles.toSet(), build = build)

fun Route.withoutRoles(vararg roles: Role, build: Route.() -> Unit) =
  authorizedRoute(none = roles.toSet(), build = build)

private fun Route.authorizedRoute(
  any: Set<Role>? = null,
  all: Set<Role>? = null,
  none: Set<Role>? = null,
  build: Route.() -> Unit
): Route {
  val description = listOfNotNull(
    any?.let { "anyOf (${any.joinToString(" ")})" },
    all?.let { "allOf (${all.joinToString(" ")})" },
    none?.let { "noneOf (${none.joinToString(" ")})" }).joinToString(",")
  val authorizedRoute = createChild(AuthorizedRouteSelector(description))
  application.plugin(RoleBasedAuthorization).interceptPipeline(authorizedRoute, any, all, none)
  authorizedRoute.build()
  return authorizedRoute
}