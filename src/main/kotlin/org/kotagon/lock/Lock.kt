package org.kotagon.lock

/**
 * The base class that represents lock.
 */
abstract class Lock

/**
 * The class provides a structured representation of the boolean compositions
 * of locks.
 *
 * The [LockExpression] is a sealed class that encapsulates a singular abstract method, [evaluate],
 * which facilitates the evaluation of Kotlin expressions to a boolean value. Several subclasses
 * derive from the [LockExpression] class, each representing distinct logical operations.
 */
sealed class LockExpression {
    abstract fun evaluate(): Boolean
    abstract override fun equals(other: Any?): Boolean

    operator fun not(): LockExpression = NotLockExpression(this)
    infix fun and(other: LockExpression) = AndLockExpression(this, other)
    infix fun or(other: LockExpression) = OrLockExpression(this, other)
}

/**
 * [NotLockExpression] represents logical negation.
 */
class NotLockExpression(
    val expr: LockExpression
): LockExpression() {
    override fun evaluate() = !expr.evaluate()

    override fun equals(other: Any?) = other is NotLockExpression && this.expr == other.expr
    override fun hashCode() = 25
}

/**
 * [AndLockExpression] represents logical conjunction.
 */
class AndLockExpression(
    val expr1: LockExpression,
    val expr2: LockExpression
): LockExpression() {
    override fun evaluate() = expr1.evaluate() && expr2.evaluate()

    override fun equals(other: Any?) = other is AndLockExpression &&
            (this.expr1 == other.expr1 && this.expr2 == other.expr2 || this.expr1 == other.expr2 && this.expr2 == other.expr1)
    override fun hashCode() = 25
}

/**
 * [AndLockExpression] represents logical disjunction.
 */
class OrLockExpression(
    val expr1: LockExpression,
    val expr2: LockExpression
): LockExpression() {
    override fun evaluate() = expr1.evaluate() || expr2.evaluate()

    override fun equals(other: Any?) = other is OrLockExpression &&
            (this.expr1 == other.expr1 && this.expr2 == other.expr2 || this.expr1 == other.expr2 && this.expr2 == other.expr1)
    override fun hashCode() = 25
}

/**
 * [TrueLockExpression] represents constant expression evaluated to logical `true` value.
 */
object TrueLockExpression : LockExpression() {
    override fun evaluate() = true
    override fun equals(other: Any?) = other is TrueLockExpression
    override fun hashCode() = 25
}
/**
 * [FalseLockExpression] represents constant expression evaluated to logical `false` value.
 */
object FalseLockExpression : LockExpression() {
    override fun evaluate() = false
    override fun equals(other: Any?) = other is FalseLockExpression
    override fun hashCode() = 25
}

/**
 * Internal class used to mock arguments in policy declarations.
 *
 * @param T argument type
 */
class LockStubArg<T> internal constructor() {}
