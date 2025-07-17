package com.rrain.util.bson

import org.bson.Document



fun String.toDoc() = Document.parse(this)
