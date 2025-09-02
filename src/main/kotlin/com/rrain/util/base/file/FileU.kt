package com.rrain.util.base.file

import kotlinx.io.files.FileNotFoundException
import java.io.InputStream
import java.nio.file.Paths



object FileU {
  
  // path relative to "resources" folder
  fun getResourceAsStream(relPath: String): InputStream = (
    object { }::class.java.getResourceAsStream("/$relPath")
      ?: throw FileNotFoundException(relPath)
    )
  
  // Get project folder absolute path or path to executing jar...
  // e.g. "D:\PROG\Kotlin\[projects]\test-kotlin"
  val absPath = Paths.get("").toAbsolutePath()
  
}