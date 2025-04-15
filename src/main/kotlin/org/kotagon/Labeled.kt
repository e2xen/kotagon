package org.kotagon

import jdk.jpackage.internal.Arguments.CLIOptions.context
import org.kotagon.exception.InformationFlowException

class Labeled<P : Policy, E> {
    // policy-protected value
    var value: E
    val policy: Policy

    internal constructor(p: P, v: E) {
        value = v
        policy = p
    }

    // set value of this to value of other, if policy allows
    fun <OtherPolicy : Policy> accept(other: Labeled<OtherPolicy, E>) {
        if (allowedToFlow(other.policy, this.policy)) {
            value = other.value
        } else {
            throw InformationFlowException()
        }
    }

    context(PolicyEvaluationContext)
    fun <OtherPolicy : Policy> accept(other: Labeled<OtherPolicy, E>) {
        TODO()
    }

    // produce new labeled from value of this
    fun <R> produce(producer: SecuredContext<P>.(E) -> R): Labeled<P, R> {
        TODO()
    }

    fun set(v: E) {
        value = v
    }

    context(SecuredContext<PolicyFrom>)
    fun <PolicyFrom> set(v: E) {
        if (allowedToFlow(this@SecuredContext.policy, this.policy)) {

        }
    }
}

fun <P : Policy, T> labeled(policy: P, builder: () -> T): Labeled<P, T> {
    return Labeled(policy, builder())
}

//inline fun <reified From : Policy, reified To : Policy> allowedToFlow() {
//    allowedToFlow(From::class, To::class)
//}

fun allowedToFlow(from: Policy, to: Policy): Boolean {
    return to.objectReceivers.all { obj ->
        from.objectReceivers.contains(obj) || from.classReceivers.any { klass ->
            klass.isInstance(obj)
        } && to.classReceivers.all { toClass ->
            from.classReceivers.any { fromClass ->
                toClass.java.isAssignableFrom(fromClass.java)
            }
        }
    }
}

class SecuredContext<P : Policy> {
    val policy: Policy
    internal constructor(p: P) {
        policy = p
    }
}

class PolicyEvaluationContext(val map: Map<LockExpression, Boolean>)

fun withPolicyEvaluationContext(vararg lockExpressions: LockExpression, f: PolicyEvaluationContext.() -> Unit) {
    PolicyEvaluationContext(buildMap {
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
