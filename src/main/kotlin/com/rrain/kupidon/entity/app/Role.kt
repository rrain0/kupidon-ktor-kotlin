package com.rrain.kupidon.entity.app

import java.lang.IllegalStateException


/*
Принципы ролей:
● 1 роут делается для 1 роли или для любой 1 роли из указанного множества ролей.
● Роли не образуют древовидную структуру наследования, одна роль не может включать другую.
 */

/*
● WANDERER (гость) - [неявная роль] - роуты без проверки аутентификации и авотризации

● USER (обычный пользователь) - [неявная роль] - роуты с проверкой аутентификации, но без проверки авторизации (или проверка с 0 ролями)

● ADMIN - нужна авторизация и наличие роли ADMIN
 */


enum class Role {
  ADMIN,
  ;
  
  init {
    if (!this.name.matches(rolePattern))
      throw IllegalStateException(
        "Role name '${this.name}' does not match '${rolePattern}' pattern."
      )
  }
}

val rolePattern = Regex("""^[a-zA-Z][a-zA-Z_\-\d]*$""")