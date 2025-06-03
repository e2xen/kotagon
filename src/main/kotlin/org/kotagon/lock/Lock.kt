package org.kotagon.lock

abstract class Lock

sealed class LockExpression {
    abstract fun evaluate(): Boolean
    abstract override fun equals(other: Any?): Boolean

    operator fun not(): LockExpression = NotLockExpression(this)
    infix fun and(other: LockExpression) = AndLockExpression(this, other)
    infix fun or(other: LockExpression) = OrLockExpression(this, other)
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
            (this.expr1 == other.expr1 && this.expr2 == other.expr2 || this.expr1 == other.expr2 && this.expr2 == other.expr1)
    override fun hashCode() = 25
}

class OrLockExpression(
    val expr1: LockExpression,
    val expr2: LockExpression
): LockExpression() {
    override fun evaluate() = expr1.evaluate() || expr2.evaluate()

    override fun equals(other: Any?) = other is OrLockExpression &&
            (this.expr1 == other.expr1 && this.expr2 == other.expr2 || this.expr1 == other.expr2 && this.expr2 == other.expr1)
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
