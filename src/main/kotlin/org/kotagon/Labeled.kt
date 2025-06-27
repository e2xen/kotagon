package org.kotagon

import org.kotagon.exception.InformationFlowException
import org.kotagon.lock.FalseLockExpression
import org.kotagon.lock.LockExpression
import org.kotagon.lock.TrueLockExpression

class Labeled<P : Policy, E> {
    companion object {
        private const val ILLEGAL_FLOW_TEMPLATE = "Illegal flow detected: '%s' -> '%s'"
    }

    // policy-protected value
    internal var value: E
    val policy: P

    internal constructor(p: P, v: E) {
        value = v
        policy = p
    }

    // set value of this to value of other, if policy allows
    context(PolicyEvaluationContext)
    fun <OtherPolicy : Policy> accept(other: Labeled<OtherPolicy, E>) {
        val from = other.policy
        val to = this.policy
        if (allowedToFlow(from, to)) {
            value = other.value
        } else {
            throw InformationFlowException(ILLEGAL_FLOW_TEMPLATE.format(from.getPolicyName(), to.getPolicyName()))
        }
    }

    // produce new labeled from value of this
    context(PolicyEvaluationContext)
    fun <R> map(mapper: context(SecuredContext<P>, PolicyEvaluationContext) (E) -> R): Labeled<P, R> {
        return labeled(policy) { mapper(SecuredContext<P>(policy), this@PolicyEvaluationContext, value) }
    }

    context(SecuredContext<PolicyFrom>, PolicyEvaluationContext)
    fun <PolicyFrom : Policy> set(v: E) {
        val from = this@SecuredContext.policy
        val to = this.policy
        if (allowedToFlow(from, to)) {
            value = v
        } else {
            throw InformationFlowException(ILLEGAL_FLOW_TEMPLATE.format(from.getPolicyName(), to.getPolicyName()))
        }
    }

    context(SecuredContext<PolicyFrom>, PolicyEvaluationContext)
    fun <PolicyFrom : Policy> get(): E {
        val from = this.policy
        val to = this@SecuredContext.policy
        if (allowedToFlow(from, to)) {
            return this.value
        } else {
            throw InformationFlowException(ILLEGAL_FLOW_TEMPLATE.format(from.getPolicyName(), to.getPolicyName()))
        }
    }

    fun unsafeGet() = value
}

fun <P : Policy, T> labeled(policy: P, builder: () -> T): Labeled<P, T> {
    return Labeled(policy, builder())
}

//inline fun <reified From : Policy, reified To : Policy> allowedToFlow() {
//    allowedToFlow(From::class, To::class)
//}

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

class SecuredContext<P : Policy> {
    val policy: P
    internal constructor(p: P) {
        policy = p
    }
}

class PolicyEvaluationContext(val lockExpressionContext: Map<LockExpression, Boolean>)

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
