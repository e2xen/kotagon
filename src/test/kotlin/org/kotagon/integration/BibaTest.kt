package org.kotagon.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.withPolicyEvaluationContext


object TrustedLevel
object UntrustedLevel
object TrustedPolicy : Policy({
    +TrustedLevel
    +UntrustedLevel
})
object UntrustedPolicy : Policy({
    +UntrustedLevel
})

class TrustedSubjectActor {
    private val data: Labeled<TrustedPolicy, String> = labeled(TrustedPolicy) {"trustedSubject"}
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

class UntrustedSubjectActor {
    private val data: Labeled<UntrustedPolicy, String> = labeled(UntrustedPolicy) {"untrustedSubject"}
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

class BibaTest {
    lateinit var trustedObject: Labeled<TrustedPolicy, String>
    lateinit var untrustedObject: Labeled<UntrustedPolicy, String>
    lateinit var trustedSubject: TrustedSubjectActor
    lateinit var untrustedSubject: UntrustedSubjectActor

    @BeforeEach
    fun before() {
        trustedObject = labeled(TrustedPolicy) { "trustedObject" }
        untrustedObject = labeled(UntrustedPolicy) { "untrustedObject" }
        trustedSubject = TrustedSubjectActor()
        untrustedSubject = UntrustedSubjectActor()
    }

    @Test
    fun test1() {
        trustedSubject.readFrom(trustedObject)
    }

    @Test
    fun test2() {
        trustedSubject.writeTo(trustedObject)
    }

    @Test
    fun test3() {
        trustedSubject.writeTo(untrustedObject)
    }

    @Test
    fun test4() {
        untrustedSubject.readFrom(untrustedObject)
    }

    @Test
    fun test5() {
        untrustedSubject.writeTo(untrustedObject)
    }

    @Test
    fun test6() {
        untrustedSubject.readFrom(trustedObject)
    }

    @Test
    fun test7() {
        assertThrows<InformationFlowException> {
            trustedSubject.readFrom(untrustedObject)
        }
    }

    @Test
    fun test8() {
        assertThrows<InformationFlowException> {
            untrustedSubject.writeTo(trustedObject)
        }
    }

}