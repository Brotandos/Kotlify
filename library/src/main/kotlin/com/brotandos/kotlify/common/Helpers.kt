package com.brotandos.kotlify.common

import java.lang.RuntimeException

open class KotlifyException(message: String = "") : RuntimeException(message)

object NoGetterException : KotlifyException("There's no getter for this property")