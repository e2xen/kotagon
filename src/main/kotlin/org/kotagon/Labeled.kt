package org.kotagon

import org.kotagon.exception.InformationFlowException
import org.kotagon.lock.FalseLockExpression
import org.kotagon.lock.LockExpression
import org.kotagon.lock.TrueLockExpression

class Labeled<P : Policy, E> {
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
        if (allowedToFlow(other.policy, this.policy)) {
            value = other.value
        } else {
            throw InformationFlowException()
        }
    }

    // produce new labeled from value of this
    context(PolicyEvaluationContext)
    fun <R> map(mapper: context(SecuredContext<P>, PolicyEvaluationContext) (E) -> R): Labeled<P, R> {
        return labeled(policy) { mapper(SecuredContext<P>(policy), this@PolicyEvaluationContext, value) }
    }

    context(SecuredContext<PolicyFrom>, PolicyEvaluationContext)
    fun <PolicyFrom : Policy> set(v: E) {
        if (allowedToFlow(this@SecuredContext.policy, this.policy)) {
            value = v
        } else {
            throw InformationFlowException()
        }
    }

    context(SecuredContext<PolicyFrom>, PolicyEvaluationContext)
    fun <PolicyFrom : Policy> get(): E {
        if (allowedToFlow(this.policy, this@SecuredContext.policy)) {
            return this.value
        } else {
            throw InformationFlowException()
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
