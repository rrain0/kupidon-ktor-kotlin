package com.rrain.util.fileutils

import kotlinx.io.files.FileNotFoundException



object FileU {
  
  // path relative to "resources" folder
  fun getResourceAsStream(relPath: String) = object { }::class.java.getResourceAsStream("/$relPath")
    ?: throw FileNotFoundException(relPath)
  
}