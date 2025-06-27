package org.kotagon.exception

class InformationFlowException : RuntimeException {
    constructor() : super()

    constructor(message: String?) : super(message)
}