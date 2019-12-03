package com.brotandos.kotlify.exception

class ContextAnonymousException : IllegalStateException(
        "context must not be an anonymous object literal"
)