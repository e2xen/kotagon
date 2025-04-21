package org.kotagon

import org.kotagon.exception.AbstractLockExpressionException

abstract class Lock

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

sealed class LockExpression {
    abstract fun evaluate(): Boolean
    abstract override fun equals(other: Any?): Boolean

    operator fun not(): LockExpression = NotLockExpression(this)
    infix fun and(other: LockExpression) = AndLockExpression(this, other)
    infix fun or(other: LockExpression) = OrLockExpression(this, other)
}

class PureUnaryLockExpression<T>(
    val lock: UnaryLock<T>,
    val arg: T?
) : LockExpression() {
    override fun evaluate(): Boolean {
        if (arg == null)
            throw AbstractLockExpressionException()
        return lock.isOpen(arg)
    }

    override fun equals(other: Any?) = other is PureUnaryLockExpression<T> && this.lock == other.lock
    override fun hashCode() = 25
}

class NotLockExpression(
    val expr: LockExpression
): LockExpression() {
    override fun evaluate() = !expr.evaluate()

    override fun equals(other: Any?) = other is NotLockExpression && this.expr == other.expr
    override fun hashCode() = 25
}

class AndLockExpression(
    val expr1: LockExpression,
    val expr2: LockExpression
): LockExpression() {
    override fun evaluate() = expr1.evaluate() && expr2.evaluate()

    override fun equals(other: Any?) = other is AndLockExpression &&
            this.expr1 == other.expr1 && this.expr2 == other.expr2
    override fun hashCode() = 25
}

class OrLockExpression(
    val expr1: LockExpression,
    val expr2: LockExpression
): LockExpression() {
    override fun evaluate() = expr1.evaluate() || expr2.evaluate()

    override fun equals(other: Any?) = other is OrLockExpression &&
            this.expr1 == other.expr1 && this.expr2 == other.expr2
    override fun hashCode() = 25
}

object TrueLockExpression : LockExpression() {
    override fun evaluate() = true
    override fun equals(other: Any?) = other is TrueLockExpression
    override fun hashCode() = 25
}
object FalseLockExpression : LockExpression() {
    override fun evaluate() = false
    override fun equals(other: Any?) = other is FalseLockExpression
    override fun hashCode() = 25
}

class LockStubArg<T> internal constructor() {}
