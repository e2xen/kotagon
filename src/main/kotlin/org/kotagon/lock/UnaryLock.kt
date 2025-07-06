package org.kotagon.lock

import org.kotagon.Labeled
import org.kotagon.exception.AbstractLockExpressionException

/**
 * An unary lock is a type of lock that accepts a single parameter as an argument.
 * It can be opened or closed based on the specified actor associated with that parameter.
 * In Kotagon, the unary locks are represented by an abstract class that includes a type parameter, [UnaryLock].
 *
 * Example of usage:
 * ```
 * object Paid : UnaryLock<Customer>()
 *
 * val customerData = customerStorage.getCustomerData(customer)
 * if (processPayment(customer)) {
 *     Paid.open(customer)
 * } else {
 *     //...
 * }
 *
 * withPolicyEvaluationContext(Paid(customer)) {
 *     // Secured context
 * }
 * ```
 *
 * @param T type of lock argument
 */
abstract class UnaryLock<in T>() : Lock() {
    private val states: MutableMap<T, Boolean> = HashMap()

    /**
     * Binds lock argument and returns [LockExpression].
     *
     * @param v lock parameter
     */
    operator fun invoke(v: T): LockExpression {
        return PureUnaryLockExpression(this, v)
    }

    /**
     * Binds lock argument from labeled and returns [LockExpression].
     *
     * Important: use this method with caution.
     * @param v data-container
     */
    operator fun invoke(v: Labeled<*, @UnsafeVariance T>) = invoke(v.unsafeGet())

    /**
     * Binds lock argument to mock and returns [LockExpression].
     *
     * @param v lock param stub
     */
    operator fun invoke(v: LockStubArg<out T>): LockExpression {
        return PureUnaryLockExpression(this, null)
    }

    /**
     * Opens lock for the passed `v`.
     *
     * @param v value
     */
    open fun open(v: T) {
        states[v] = true
    }

    /**
     * Closes lock for the passed `v`.
     *
     * @param v value
     */
    open fun close(v: T) {
        states[v] = false
    }

    /**
     * Checks if the lock is open for passed `v`.
     *
     * @param v value
     * @return `true` if the lock is opened, `false` otherwise.
     */
    open fun isOpen(v: T): Boolean {
        return states[v] == true
    }
    /**
     * Checks if the lock is closed for passed `v`.
     *
     * @param v value
     * @return `true` if the lock is closed, `false` otherwise.
     */
    fun isClosed(v: T) = !isOpen(v)
}

/**
 * Another category of [LockExpression] subclasses is those encapsulating locks.
 * [PureUnaryLockExpression] encapsulates [UnaryLock] with its argument.
 *
 * @param lock unary lock
 * @param arg lock argument
 */
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