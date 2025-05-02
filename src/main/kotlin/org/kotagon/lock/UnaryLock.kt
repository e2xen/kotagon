package org.kotagon.lock

import org.kotagon.exception.AbstractLockExpressionException

abstract class UnaryLock<in T>() : Lock() {
    private val states: MutableMap<T, Boolean> = HashMap()

    operator fun invoke(v: T): LockExpression {
        return PureUnaryLockExpression(this, v)
    }

    operator fun invoke(v: LockStubArg<out T>): LockExpression {
        return PureUnaryLockExpression(this, null)
    }

    fun open(v: T) {
        states[v] = true
    }
    fun close(v: T) {
        states[v] = false
    }

    fun isOpen(v: T): Boolean {
        return states[v] == true
    }
    fun isClosed(v: T) = !isOpen(v)
}

class PureUnaryLockExpression<T>(
    val lock: UnaryLock<T>,
    val arg: T?
) : LockExpression() {
    override fun evaluate(): Boolean {
        if (arg == null)
            throw AbstractLockExpressionException("Provided argument to UnaryLock ($lock) is null")
        return lock.isOpen(arg)
    }

    override fun equals(other: Any?) = other is PureUnaryLockExpression<T> && this.lock == other.lock
    override fun hashCode() = 25
}