package com.rrain.util.base.print



fun println(vararg args: Any?) = kotlin.io.println(args.joinToString(" "))