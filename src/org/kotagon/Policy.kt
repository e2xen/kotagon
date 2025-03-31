package org.kotagon

import org.kotagon.PolicyBuilderContext

abstract class Policy {
    val policyReceivers: List<Any>
    constructor(builder: PolicyBuilderContext.() -> Unit) {
        val ctx = PolicyBuilderContext().apply(builder)
        policyReceivers = ctx.policyReceivers
        //...
    }
}

class PolicyBuilderContext internal constructor() {
    val policyReceivers = ArrayList<Any>()
    operator fun Any.unaryPlus() {
        policyReceivers.add(this)
    }
}