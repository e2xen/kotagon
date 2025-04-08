package org.kotagon

import java.util.function.BiFunction
import kotlin.reflect.KClass

abstract class Policy {
    val objectReceivers: List<Any>
    val classReceivers: List<KClass<out Any>>

    constructor(builder: PolicyBuilderContext.() -> Unit) {
        val ctx = PolicyBuilderContext().apply(builder)
        objectReceivers = ctx.objectReceivers
        classReceivers = ctx.classReceivers
        //...
    }
}

class PolicyBuilderContext internal constructor() {
    internal val objectReceivers = ArrayList<Any>()
    internal val classReceivers = ArrayList<KClass<out Any>>()
    internal val predicates = ArrayList<(Nothing) -> Boolean>()

    operator fun Any.unaryPlus() {
        objectReceivers.add(this)
    }
    fun any(klass: KClass<out Any>) {
        classReceivers.add(klass)
    }
    inline fun <reified T : Any> any() {
        any(T::class)
    }
    fun <T> suchThat(predicate: (T) -> Boolean) {
        predicates.add(predicate)
        predicates[0].javaClass.
    }
}