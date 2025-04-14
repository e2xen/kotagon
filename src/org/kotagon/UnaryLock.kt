package org.kotagon


abstract class UnaryLock<T>() {
    val states: MutableMap<T, Boolean> = HashMap()

    operator fun invoke(v: T): LockExpression {
        return LockExpression(this as UnaryLock<Any>, v as Any)
    }

    operator fun invoke(v: LockArg<T>): ParametrizedLockExpression {

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

open class Expression {}

class LockExpression(val lock: UnaryLock<Any>, val arg: Any) : Expression() {
    fun evaluate(): Boolean {
        return lock.isOpen(arg)
    }
}

class ParametrizedLockExpression : Expression() {}

class LockArg<T> {}
