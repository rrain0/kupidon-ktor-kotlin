package com.rrain.utils.bson

import org.bson.Document



fun String.toDoc() = Document.parse(this)
