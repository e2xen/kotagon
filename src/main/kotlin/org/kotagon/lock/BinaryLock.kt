package org.kotagon.lock

import org.kotagon.exception.AbstractLockExpressionException

/**
 * The binary lock is similar to the unary lock, the difference is only that it accepts two parameter as arguments.
 * It can be opened or closed on the specific actors associated with the parameters. In Kotagon, the binary lock
 * is represented by an abstract class with two typed parameters, [BinaryLock].
 *
 * Example of usage:
 * ```
 * private object Read : BinaryLock<Member, File>()
 * private object Write : BinaryLock<Member, File>()
 *
 * withPolicyEvaluationContext(Read(member, file) or Write(member, file)) {
 *     // Secured context
 * }
 * ```
 *
 * @param T1 type of first lock argument
 * @param T2 type of second lock argument
 */
abstract class BinaryLock<in T1, in T2> : Lock() {
    private val states: MutableMap<Pair<T1, T2>, Boolean> = HashMap()

    /**
     * Binds lock arguments and returns [LockExpression].
     *
     * @param v1 first lock parameter
     * @param v2 second lock parameter
     */
    operator fun invoke(v1: T1, v2: T2): LockExpression {
        return PureBinaryLockExpression(this, v1, v2)
    }

    /**
     * Binds lock arguments as mocks and returns [LockExpression].
     *
     * @param v1 first lock param stub
     * @param v2 second lock param stub
     */
    operator fun invoke(v1: LockStubArg<out T1>, v2: LockStubArg<out T2>): LockExpression {
        return PureBinaryLockExpression(this, null, null)
    }

    /**
     * Binds first lock arg and second arg as stub and returns [LockExpression].
     *
     * @param v1 first lock parameter
     * @param v2 second lock param stub
     */
    operator fun invoke(v1: T1, v2: LockStubArg<out T2>): LockExpression {
        return PureBinaryLockExpression(this, v1, null)
    }

    /**
     * Binds first lock arg as stub and second arg and returns [LockExpression].
     *
     * @param v1 first lock parameter
     * @param v2 second lock param stub
     */
    operator fun invoke(v1: LockStubArg<out T1>, v2: T2): LockExpression {
        return PureBinaryLockExpression(this, null, v2)
    }

    /**
     * Opens lock for the passed values.
     *
     * @param v1 first lock param
     * @param v2 second lock param
     */
    fun open(v1: T1, v2: T2) {
        states[Pair(v1, v2)] = true
    }

    /**
     * Closes lock for the passed values.
     *
     * @param v1 first lock param
     * @param v2 second lock param
     */
    fun close(v1: T1, v2: T2) {
        states[Pair(v1, v2)] = false
    }

    /**
     * Checks if the lock is open for passed values.
     *
     * @param v1 first lock param
     * @param v2 second lock param
     * @return `true` if the lock is opened, `false` otherwise.
     */
    fun isOpen(v1: T1, v2: T2): Boolean {
        return states[Pair(v1, v2)] == true
    }

    /**
     * Checks if the lock is closed for passed values.
     *
     * @param v1 first lock param
     * @param v2 second lock param
     * @return `true` if the lock is opened, `false` otherwise.
     */
    fun isClosed(v1: T1, v2: T2): Boolean = !isOpen(v1, v2)
}

/**
 * Another category of [LockExpression] subclasses is those encapsulating locks.
 * [PureBinaryLockExpression] encapsulates [BinaryLock] with its arguments.
 *
 * @param lock binary lock
 * @param arg1 first lock argument
 * @param arg2 second lock argument
 */
class PureBinaryLockExpression<T1, T2>(
    val lock: BinaryLock<T1, T2>,
    val arg1: T1?,
    val arg2: T2?
): LockExpression() {
    override fun evaluate(): Boolean {
        if (arg1 == null || arg2 == null) {
            throw AbstractLockExpressionException("Provided arg1 or arg2 to BinaryLock ($lock) is null")
        }
        return lock.isOpen(arg1, arg2)
    }

    override fun equals(other: Any?): Boolean = other is PureBinaryLockExpression<T1, T2> && this.lock == other.lock

    override fun hashCode(): Int = 25
}
