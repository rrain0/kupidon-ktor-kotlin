package com.rrain.kupidon.route.route.test

import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.post
import io.ktor.server.routing.put



fun Application.typeSafeRoutingTest() {
  
  // Plugin for using of Typed Routes (Resources)
  // It is already installed in Routing.kt.
  //install(Resources)
  
  
  
  routing {
    
    // http://localhost:40040/articles
    get<Articles> { article ->
      // Get all articles ...
      call.respondText("List of articles sorted starting from ${article.sort}")
    }
    // http://localhost:40040/articles/new
    get<Articles.New> {
      // Show a page with fields for creating a new article ...
      call.respondText("Create a new article")
    }
    post<Articles> {
      // Save an article ...
      call.respondText("An article is saved", status = HttpStatusCode.Created)
    }
    // http://localhost:40040/articles/123
    get<Articles.Id> { article ->
      // Show an article with id ${article.id} ...
      call.respondText("An article with id ${article.id}", status = HttpStatusCode.OK)
    }
    // http://localhost:40040/articles/123/edit
    get<Articles.Id.Edit> { article ->
      // Show a page with fields for editing an article ...
      call.respondText("Edit an article with id ${article.parent.id}", status = HttpStatusCode.OK)
    }
    put<Articles.Id> { article ->
      // Update an article ...
      call.respondText("An article with id ${article.id} updated", status = HttpStatusCode.OK)
    }
    delete<Articles.Id> { article ->
      // Delete an article ...
      call.respondText("An article with id ${article.id} deleted", status = HttpStatusCode.OK)
    }
  
  }
  
  
  
}




@Resource("/articles")
private class Articles(val sort: String? = "new") {
  
  @Resource("new")
  class New(val parent: Articles = Articles())
  
  @Resource("{id}")
  class Id(val parent: Articles = Articles(), val id: Long) {
    @Resource("edit")
    class Edit(val parent: Id)
  }
  
}


