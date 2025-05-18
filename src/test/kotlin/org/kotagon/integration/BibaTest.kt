package org.kotagon.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.withPolicyEvaluationContext


private object TrustedLevel
private object UntrustedLevel
private object TrustedPolicy : Policy({
    +TrustedLevel
    +UntrustedLevel
})
private object UntrustedPolicy : Policy({
    +UntrustedLevel
})

private class TrustedSubjectActor {
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

private class UntrustedSubjectActor {
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
    private lateinit var trustedObject: Labeled<TrustedPolicy, String>
    private lateinit var untrustedObject: Labeled<UntrustedPolicy, String>
    private lateinit var trustedSubject: TrustedSubjectActor
    private lateinit var untrustedSubject: UntrustedSubjectActor

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