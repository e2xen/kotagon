package org.kotagon.exception

import org.kotagon.Policy

class InformationFlowException : RuntimeException {
    constructor() : super()

    constructor(message: String?) : super(message)
    constructor(from: Policy, to: Policy) : super(ILLEGAL_FLOW_TEMPLATE.format(from.getPolicyName(), to.getPolicyName()))
    companion object {
        private const val ILLEGAL_FLOW_TEMPLATE = "Illegal flow detected: '%s' -> '%s'"
    }
}