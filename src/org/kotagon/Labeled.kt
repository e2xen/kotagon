package org.kotagon

class Labeled<P : Policy, E> {
    // policy-protected value
    var value: E

    internal constructor(v: E) {
        value = v
    }

    // set value of this to value of other, if policy allows
    fun <OtherPolicy : Policy> accept(other: Labeled<OtherPolicy, E>) {
        TODO()
    }

    // produce new labeled from value of this
    fun <R> map(mapper: SecuredContext<P>.(E) -> R): Labeled<P, R> {
        TODO()
    }

    fun set(v: E) {
        value = v
    }

    context(SecuredContext<PolicyFrom>)
    fun <PolicyFrom> set(v: E) {
        TODO()
    }
}

class SecuredContext<P : Policy> {}

// move to LabeledExtensions.kt
fun <P : Policy> Labeled<P, Boolean>.ifTrue(action: SecuredContext<P>.() -> Unit) {
    TODO()
}

fun <P : Policy> Labeled<P, Boolean>.ifFalse(action: SecuredContext<P>.() -> Unit) {
    TODO()
}
