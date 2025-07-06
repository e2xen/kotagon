package org.kotagon

import org.kotagon.lock.LockExpression
import org.kotagon.lock.LockStubArg
import org.kotagon.lock.TrueLockExpression
import kotlin.reflect.KClass

/**
 * The root of the class hierarchy for policies. Every policy class has [Policy] as a superclass.
 *
 * Example of static policy declaration:
 * ```
 * object AnyCustomer : Policy({
 *     any<Customer>() // same as suchThat<Customer> { true }
 * })
 * ```
 *
 * Example of policy declaration with parameters:
 * ```
 * class SpecificCustomer(customer: Customer) : Policy({
 *   +customer
 * })
 * ```
 */
abstract class Policy {
    val objectReceivers: List<Any>
    val lockedReceivers: List<LockedReceiver>

    constructor(builder: PolicyBuilderContext.() -> Unit) {
        val ctx = PolicyBuilderContext().apply(builder)
        objectReceivers = ctx.objectReceivers
        lockedReceivers = ctx.lockedReceivers
    }

    fun getPolicyName(): String? = this::class.qualifiedName
}

/**
 * Internal specialized wrapper used to construct policy expressions.
 * Used to construct policies [Policy].
 */
data class LockedReceiver(
    val receiverClass: KClass<out Any>,
    val lock: LockExpression
)

/**
 * Special builder for policies.
 * Used to construct policies [Policy].
 */
class PolicyBuilderContext internal constructor() {
    internal val objectReceivers = ArrayList<Any>()
    internal val lockedReceivers = ArrayList<LockedReceiver>()

    operator fun Any.unaryPlus() {
        objectReceivers.add(this)
    }
    fun any(klass: KClass<out Any>) {
        lockedReceivers.add(LockedReceiver(klass, TrueLockExpression))
    }
    inline fun <reified T : Any> any() {
        any(T::class)
    }
    fun <T> suchThat(klass: KClass<out Any>, predicate: (LockStubArg<T>) -> LockExpression) {
        lockedReceivers.add(LockedReceiver(klass, predicate.invoke(LockStubArg())))
    }
    inline fun <reified T : Any> suchThat(noinline predicate: (LockStubArg<T>) -> LockExpression) {
        suchThat(T::class, predicate)
    }
}