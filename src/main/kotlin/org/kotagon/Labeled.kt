package org.kotagon

import org.kotagon.exception.InformationFlowException

class Labeled<P : Policy, E> {
    // policy-protected value
    var value: E
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

//    context(PolicyEvaluationContext)
//    fun <OtherPolicy : Policy> accept(other: Labeled<OtherPolicy, E>) {
//        TODO()
//    }

    // produce new labeled from value of this
    context(PolicyEvaluationContext)
    fun <R> map(mapper: context(SecuredContext<P>, PolicyEvaluationContext) (E) -> R): Labeled<P, R> {
        return labeled(policy) { mapper(SecuredContext<P>(policy), this@PolicyEvaluationContext, value) }
    }

    fun set(v: E) {
        value = v
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

fun withPolicyEvaluationContext(vararg lockExpressions: LockExpression, f: PolicyEvaluationContext.() -> Unit) {
    PolicyEvaluationContext(buildMap {
        put(TrueLockExpression, true)
        put(FalseLockExpression, false)
        lockExpressions.forEach {
            put(it, it.evaluate())
        }
    }).also { it.f() }
}

// move to LabeledExtensions.kt
fun <P : Policy> Labeled<P, Boolean>.ifTrue(action: SecuredContext<P>.() -> Unit) {
    TODO()
}

fun <P : Policy> Labeled<P, Boolean>.ifFalse(action: SecuredContext<P>.() -> Unit) {
    TODO()
}
