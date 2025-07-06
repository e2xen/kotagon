package org.kotagon.exception

import org.kotagon.Policy

/**
 * Base exception.
 *
 * Thrown when illegal information flow is detected.
 */
class InformationFlowException : RuntimeException {
    companion object {
        private const val ILLEGAL_FLOW_TEMPLATE = "Illegal flow detected: '%s' -> '%s'"
    }

    constructor() : super()

    constructor(message: String?) : super(message)
    constructor(from: Policy, to: Policy) : super(ILLEGAL_FLOW_TEMPLATE.format(from.getPolicyName(), to.getPolicyName()))
}