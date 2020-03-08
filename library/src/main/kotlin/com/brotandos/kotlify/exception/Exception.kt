package com.brotandos.kotlify.exception

class ContextAnonymousException : IllegalStateException(
        "context must not be an anonymous object literal"
)

class PathInTreeIgnoredException : IllegalStateException(
        "You should provide path in ui tree"
)

class WidgetNotBuiltException(functionName: String) : IllegalStateException(
        "Widget should built before [$functionName] function called"
)