package com.rrain.utils.base.print



fun println(vararg args: Any?) = kotlin.io.println(args.joinToString(" "))