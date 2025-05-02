package org.kotagon.exception

class AbstractLockExpressionException : RuntimeException {
    constructor(): super()
    constructor(message: String?): super(message)
}