package org.kotagon.lock

import org.kotagon.exception.AbstractLockExpressionException

abstract class BinaryLock<in T1, in T2> : Lock() {
    private val states: MutableMap<Pair<T1, T2>, Boolean> = HashMap()

    operator fun invoke(v1: T1, v2: T2): LockExpression {
        return PureBinaryLockExpression(this, v1, v2)
    }
    operator fun invoke(v1: LockStubArg<out T1>, v2: LockStubArg<out T2>): LockExpression {
        return PureBinaryLockExpression(this, null, null)
    }
    operator fun invoke(v1: T1, v2: LockStubArg<out T2>): LockExpression {
        return PureBinaryLockExpression(this, v1, null)
    }
    operator fun invoke(v1: LockStubArg<out T1>, v2: T2): LockExpression {
        return PureBinaryLockExpression(this, null, v2)
    }

    fun open(v1: T1, v2: T2) {
        states[Pair(v1, v2)] = true
    }
    fun close(v1: T1, v2: T2) {
        states[Pair(v1, v2)] = false
    }

    fun isOpen(v1: T1, v2: T2): Boolean {
        return states[Pair(v1, v2)] == true
    }
    fun isClosed(v1: T1, v2: T2): Boolean = !isOpen(v1, v2)
}

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
