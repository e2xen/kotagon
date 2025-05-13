package org.kotagon.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.withPolicyEvaluationContext


object HighLevel
object LowLevel
object HighPolicy : Policy({
    +HighLevel
})
object LowPolicy : Policy({
    +HighLevel
    +LowLevel
})

class HighSubjectActor {
    private val data: Labeled<HighPolicy, String> = labeled(HighPolicy) {"highSubject"}
    fun readFrom(obj: Labeled<*, String>) {
        withPolicyEvaluationContext {
            data.accept(obj)
        }
    }
    fun writeTo(obj: Labeled<*, String>) {
        withPolicyEvaluationContext {
            obj.accept(data)
        }
    }
}

class LowSubjectActor {
    private val data: Labeled<LowPolicy, String> = labeled(LowPolicy) {"lowSubject"}
    fun readFrom(obj: Labeled<*, String>) {
        withPolicyEvaluationContext {
            data.accept(obj)
        }
    }

    fun writeTo(obj: Labeled<*, String>) {
        withPolicyEvaluationContext {
            obj.accept(data)
        }
    }
}

class BellLaPadulaTest {
    lateinit var highObject: Labeled<HighPolicy, String>
    lateinit var lowObject: Labeled<LowPolicy, String>
    lateinit var highSubject: HighSubjectActor
    lateinit var lowSubject: LowSubjectActor

    @BeforeEach
    fun before() {
        highObject = labeled(HighPolicy) { "highObject" }
        lowObject = labeled(LowPolicy) { "lowObject" }
        highSubject = HighSubjectActor()
        lowSubject = LowSubjectActor()
    }

    @Test
    fun test1() {
        highSubject.readFrom(highObject)
    }

    @Test
    fun test2() {
        highSubject.writeTo(highObject)
    }

    @Test
    fun test3() {
        highSubject.readFrom(lowObject)
    }

    @Test
    fun test4() {
        lowSubject.readFrom(lowObject)
    }

    @Test
    fun test5() {
        lowSubject.writeTo(lowObject)
    }

    @Test
    fun test6() {
        lowSubject.writeTo(highObject)
    }

    @Test
    fun test7() {
        assertThrows<InformationFlowException> {
            lowSubject.readFrom(highObject)
        }
    }

    @Test
    fun test8() {
        assertThrows<InformationFlowException> {
            highSubject.writeTo(lowObject)
        }
    }

}