package com.brotandos.kotlify.processor

import javax.annotation.processing.ProcessingEnvironment

object Utils {

    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

    const val WIDGET_CONTAINER_GENERIC_LAYOUT = 0

    const val WIDGET_CONTAINER_GENERIC_LAYOUT_PARAMS = 1
}

fun ProcessingEnvironment.printError(message: String) =
        messager.printMessage(javax.tools.Diagnostic.Kind.ERROR, message)

fun ProcessingEnvironment.printNote(message: String) =
        messager.printMessage(javax.tools.Diagnostic.Kind.NOTE, message)

fun ProcessingEnvironment.printWarning(message: String) =
        messager.printMessage(javax.tools.Diagnostic.Kind.WARNING, message)