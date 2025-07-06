package org.kotagon

import org.kotagon.exception.InformationFlowException
import org.kotagon.lock.FalseLockExpression
import org.kotagon.lock.LockExpression
import org.kotagon.lock.TrueLockExpression

/**
 * Data containers of different types (such as objects, files, and others) are categorized based on the
 * information that they contain. Data containers have labels that indicate the sensitivity of the information
 * they hold. This container encapsulates operations on stored data. In the context of Kotagon, data is stored in
 * a specialized data container represented by the class [Labeled]. The advantage of having policy as part of the
 * type definition is that data can be freely transferred between variables of the same type. Moreover,
 * the compiler performs checks the compatibility of the policies when determining the compatibility of the types.
 *
 * It is important to note that the [Labeled] class in Kotagon operates as a monad enabling the
 * sequential composition of operations on the underlying value, while enforcing a security policy on these operations.
 *
 * @param P policy of the container.
 * @param E data type stored within the container.
 */
class Labeled<P : Policy, E> {
    // policy-protected value
    internal var value: E
    val policy: P

    internal constructor(p: P, v: E) {
        value = v
        policy = p
    }

    /**
     * Sets value of this to value of other, if policy allows.
     * @param other other data-container
     */
    context(PolicyEvaluationContext)
    fun <OtherPolicy : Policy> accept(other: Labeled<OtherPolicy, E>) {
        val from = other.policy
        val to = this.policy
        if (allowedToFlow(from, to)) {
            value = other.value
        } else {
            throw InformationFlowException(from, to)
        }
    }

    /**
     * Produce new labeled from value of this.
     * @param mapper function to be applied for the value
     */
    context(PolicyEvaluationContext)
    fun <R> map(mapper: context(SecuredContext<P>, PolicyEvaluationContext) (E) -> R): Labeled<P, R> {
        return labeled(policy) { mapper(SecuredContext<P>(policy), this@PolicyEvaluationContext, value) }
    }

    /**
     * Switch from current policy to [newPolicy], if current policy allows.
     * @param newPolicy target policy
     */
    context(PolicyEvaluationContext)
    fun <PNew : Policy> switch(newPolicy: PNew): Labeled<PNew, E> {
        val from = policy
        val to = newPolicy
        if (allowedToFlow(from, to)) {
            return labeled(newPolicy) { value }
        } else {
            throw InformationFlowException(from, to)
        }
    }

    /**
     * Sets a new value to the current labeled, if policy allows.
     * @param v new value
     */
    context(SecuredContext<PolicyFrom>, PolicyEvaluationContext)
    fun <PolicyFrom : Policy> set(v: E) {
        val from = this@SecuredContext.policy
        val to = this.policy
        if (allowedToFlow(from, to)) {
            value = v
        } else {
            throw InformationFlowException(from, to)
        }
    }

    /**
     * Returns current value, if policy allows.
     *
     * Important: use this method with caution.
     */
    context(SecuredContext<PolicyFrom>, PolicyEvaluationContext)
    fun <PolicyFrom : Policy> get(): E {
        val from = this.policy
        val to = this@SecuredContext.policy
        if (allowedToFlow(from, to)) {
            return this.value
        } else {
            throw InformationFlowException(from, to)
        }
    }

    /**
     * Returns current value without policy checking.
     *
     * Important: use this method with caution.
     */
    fun unsafeGet() = value
}

/**
 * Simple builder for labeled.
 * Returns a new [Labeled] instance.
 */
fun <P : Policy, T> labeled(policy: P, builder: () -> T): Labeled<P, T> {
    return Labeled(policy, builder())
}

/**
 * Internal function to check that the data flow is allowed between two policies.
 *
 * @param from source policy
 * @param to destination policy
 */
context(PolicyEvaluationContext)
fun allowedToFlow(from: Policy, to: Policy): Boolean {
    return to.objectReceivers.all { obj ->
        from.objectReceivers.contains(obj) || from.lockedReceivers.any { locked ->
            locked.receiverClass.isInstance(obj) && lockExpressionContext[locked.lock] == true
        }
    } && to.lockedReceivers.all { toLocked ->
        from.lockedReceivers.any { fromLocked ->
            toLocked.receiverClass.java.isAssignableFrom(fromLocked.receiverClass.java) &&
                    (toLocked.lock == fromLocked.lock ||
                            lockExpressionContext[toLocked.lock] == true && lockExpressionContext[fromLocked.lock] == true)
        }
    }
}

/**
 * Internal class-wrapper for the policy.
 * @param p policy
 */
class SecuredContext<P : Policy> internal constructor(p: P) {
    val policy: P = p
}

/**
 * The [PolicyEvaluationContext] maintains a runtime mapping between [LockExpression]s and
 * their evaluated boolean states, while [LockExpression] provides a structured representation of
 * the boolean compositions of locks.
 *
 * @param lockExpressionContext mapping
 */
class PolicyEvaluationContext(val lockExpressionContext: Map<LockExpression, Boolean>)

/**
 * The [withPolicyEvaluationContext] function combines
 * [LockExpression] and [PolicyEvaluationContext] to create a controlled execution environment.
 * It takes one or more [LockExpression] parameters - where each [org.kotagon.lock.Lock] with its specified arguments
 * forms a [LockExpression] - evaluates their current state, and establishes an immutable policy context.
 * Within this context, the provided lambda function can operate while being governed by the evaluated
 * security policies.
 *
 * @param lockExpressions locks with its specified arguments
 * @param f lambda function
 */
fun <T> withPolicyEvaluationContext(vararg lockExpressions: LockExpression, f: PolicyEvaluationContext.() -> T): T {
    val ctx = PolicyEvaluationContext(buildMap {
        put(TrueLockExpression, true)
        put(FalseLockExpression, false)
        lockExpressions.forEach {
            put(it, it.evaluate())
        }
    })
    return ctx.f()
}
