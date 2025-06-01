package org.kotagon

import org.kotagon.lock.LockExpression
import org.kotagon.lock.LockStubArg
import org.kotagon.lock.TrueLockExpression
import kotlin.reflect.KClass

abstract class Policy {
    val objectReceivers: List<Any>
    val lockedReceivers: List<LockedReceiver>

    constructor(builder: PolicyBuilderContext.() -> Unit) {
        val ctx = PolicyBuilderContext().apply(builder)
        objectReceivers = ctx.objectReceivers
        lockedReceivers = ctx.lockedReceivers
    }

    override fun toString(): String {
        return "Policy{objectReceivers=$objectReceivers, lockedReceivers=$lockedReceivers}"
    }
}

data class LockedReceiver(
    val receiverClasses: List<KClass<out Any>>,
    val lock: LockExpression
)

class PolicyBuilderContext internal constructor() {
    internal val objectReceivers = ArrayList<Any>()
    internal val lockedReceivers = ArrayList<LockedReceiver>()

    operator fun Any.unaryPlus() {
        objectReceivers.add(this)
    }
    fun any(klass: KClass<out Any>) {
        lockedReceivers.add(LockedReceiver(listOf(klass), TrueLockExpression))
    }
    inline fun <reified T : Any> any() {
        any(T::class)
    }
    fun <T> suchThat(klass: KClass<out Any>, predicate: (LockStubArg<T>) -> LockExpression) {
        lockedReceivers.add(LockedReceiver(listOf(klass), predicate.invoke(LockStubArg())))
    }
    fun <T1, T2> suchThat(klass1: KClass<out Any>, klass2: KClass<out Any>, predicate: (LockStubArg<T1>, LockStubArg<T2>) -> LockExpression) {
        lockedReceivers.add(LockedReceiver(listOf(klass1, klass2), predicate.invoke(LockStubArg(), LockStubArg())))
    }
    inline fun <reified T : Any> suchThat(noinline predicate: (LockStubArg<T>) -> LockExpression) {
        suchThat(T::class, predicate)
    }
    inline fun <reified T1 : Any, reified T2 : Any> suchThat(noinline predicate: (LockStubArg<T1>, LockStubArg<T2>) -> LockExpression) {
        suchThat(T1::class, T2::class, predicate)
    }
}