package com.rrain.utils.base.logger

import org.slf4j.Logger
import org.slf4j.LoggerFactory


inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)