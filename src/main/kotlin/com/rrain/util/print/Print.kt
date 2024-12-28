package com.rrain.util.print



fun println(vararg args: Any?) = kotlin.io.println(args.joinToString(" "))