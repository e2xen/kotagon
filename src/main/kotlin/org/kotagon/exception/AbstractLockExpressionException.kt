package org.kotagon.exception

/**
 * Base lock exception.
 *
 * Thrown when incorrect arguments are provided to [org.kotagon.lock.LockExpression].
 */
class AbstractLockExpressionException : RuntimeException {
    constructor(): super()
    constructor(message: String?): super(message)
}