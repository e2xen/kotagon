package org.kotagon.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kotagon.Labeled
import org.kotagon.Policy
import org.kotagon.exception.InformationFlowException
import org.kotagon.labeled
import org.kotagon.withPolicyEvaluationContext

// Multiple levels of security
private object TopSecret
private object Secret
private object Confidential
private object SensitiveUnclassified
private object Unclassified

// Policies
private object TopSecretPolicy : Policy({
    +TopSecret
})
private object SecretPolicy : Policy({
    +TopSecret
    +Secret
})
private object ConfidentialPolicy : Policy({
    +TopSecret
    +Secret
    +Confidential
})
private object SensitiveUnclassifiedPolicy : Policy({
    +TopSecret
    +Secret
    +Confidential
    +SensitiveUnclassified
})
private object UnclassifiedPolicy : Policy({
    +TopSecret
    +Secret
    +Confidential
    +SensitiveUnclassified
    +Unclassified
})

// Actors
private class TopSecretSubjectActor : SubjectActor<TopSecretPolicy>(labeled(TopSecretPolicy) { "topSecretSubject" })
private class SecretSubjectActor : SubjectActor<SecretPolicy>(labeled(SecretPolicy) { "secretSubject" })
private class ConfidentialSubjectActor :
    SubjectActor<ConfidentialPolicy>(labeled(ConfidentialPolicy) { "confidentialSubject" })
private class SensitiveUnclassifiedSubjectActor :
    SubjectActor<SensitiveUnclassifiedPolicy>(labeled(SensitiveUnclassifiedPolicy) { "sensitiveUnclassifiedSubject" })
private class UnclassifiedSubjectActor :
    SubjectActor<UnclassifiedPolicy>(labeled(UnclassifiedPolicy) { "unclassifiedSubject" })

private abstract class SubjectActor<T : Policy>(private val data: Labeled<T, String>) {
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

class ExtendedBellLaPadulaTest {
    private lateinit var topSecretObject: Labeled<TopSecretPolicy, String>
    private lateinit var secretObject: Labeled<SecretPolicy, String>
    private lateinit var confidentialObject: Labeled<ConfidentialPolicy, String>
    private lateinit var sensitiveUnclassifiedObject: Labeled<SensitiveUnclassifiedPolicy, String>
    private lateinit var unclassifiedObject: Labeled<UnclassifiedPolicy, String>
    private lateinit var topSecretSubject: TopSecretSubjectActor
    private lateinit var secretSubject: SecretSubjectActor
    private lateinit var confidentialSubject: ConfidentialSubjectActor
    private lateinit var sensitiveUnclassifiedSubject: SensitiveUnclassifiedSubjectActor
    private lateinit var unclassifiedSubject: UnclassifiedSubjectActor

    @BeforeEach
    fun before() {
        topSecretObject = labeled(TopSecretPolicy) { "topSecretObject" }
        secretObject = labeled(SecretPolicy) { "secretObject" }
        confidentialObject = labeled(ConfidentialPolicy) { "confidentialObject" }
        sensitiveUnclassifiedObject = labeled(SensitiveUnclassifiedPolicy) { "sensitiveUnclassifiedObject" }
        unclassifiedObject = labeled(UnclassifiedPolicy) { "unclassifiedObject" }
        topSecretSubject = TopSecretSubjectActor()
        secretSubject = SecretSubjectActor()
        confidentialSubject = ConfidentialSubjectActor()
        sensitiveUnclassifiedSubject = SensitiveUnclassifiedSubjectActor()
        unclassifiedSubject = UnclassifiedSubjectActor()
    }

    @Test
    fun testLegalFlowTopSecretSubject() {
        topSecretSubject.readFrom(topSecretObject)
        topSecretSubject.writeTo(topSecretObject)

        topSecretSubject.readFrom(secretObject)
        topSecretSubject.readFrom(confidentialObject)
        topSecretSubject.readFrom(sensitiveUnclassifiedObject)
        topSecretSubject.readFrom(unclassifiedObject)
    }

    @Test
    fun testLegalFlowSecretSubject() {
        secretSubject.readFrom(secretObject)
        secretSubject.writeTo(secretObject)

        secretSubject.readFrom(confidentialObject)
        secretSubject.readFrom(sensitiveUnclassifiedObject)
        secretSubject.readFrom(unclassifiedObject)

        secretSubject.writeTo(topSecretObject)
    }

    @Test
    fun testLegalFlowConfidentialSubject() {
        confidentialSubject.readFrom(confidentialObject)
        confidentialSubject.writeTo(confidentialObject)

        confidentialSubject.readFrom(sensitiveUnclassifiedObject)
        confidentialSubject.readFrom(unclassifiedObject)

        confidentialSubject.writeTo(topSecretObject)
        confidentialSubject.writeTo(secretObject)
    }

    @Test
    fun testLegalFlowSensitiveUnclassifiedSubject() {
        sensitiveUnclassifiedSubject.readFrom(sensitiveUnclassifiedObject)
        sensitiveUnclassifiedSubject.writeTo(sensitiveUnclassifiedObject)

        sensitiveUnclassifiedSubject.readFrom(unclassifiedObject)

        sensitiveUnclassifiedSubject.writeTo(topSecretObject)
        sensitiveUnclassifiedSubject.writeTo(secretObject)
        sensitiveUnclassifiedSubject.writeTo(confidentialObject)
    }

    @Test
    fun testLegalFlowUnclassifiedSubject() {
        unclassifiedSubject.readFrom(unclassifiedObject)
        unclassifiedSubject.writeTo(unclassifiedObject)

        unclassifiedSubject.writeTo(topSecretObject)
        unclassifiedSubject.writeTo(secretObject)
        unclassifiedSubject.writeTo(confidentialObject)
        unclassifiedSubject.writeTo(sensitiveUnclassifiedObject)
    }

    @Test
    fun testIllegalFlowTopSecretSubject() {
        assertThrows<InformationFlowException> {
            topSecretSubject.writeTo(secretObject)
        }
        assertThrows<InformationFlowException> {
            topSecretSubject.writeTo(confidentialObject)
        }
        assertThrows<InformationFlowException> {
            topSecretSubject.writeTo(sensitiveUnclassifiedObject)
        }
        assertThrows<InformationFlowException> {
            topSecretSubject.writeTo(unclassifiedObject)
        }
    }

    @Test
    fun testIllegalFlowSecretSubject() {
        assertThrows<InformationFlowException> {
            secretSubject.writeTo(confidentialObject)
        }
        assertThrows<InformationFlowException> {
            secretSubject.writeTo(sensitiveUnclassifiedObject)
        }
        assertThrows<InformationFlowException> {
            secretSubject.writeTo(unclassifiedObject)
        }
    }

    @Test
    fun testIllegalFlowConfidentialSubject() {
        assertThrows<InformationFlowException> {
            confidentialSubject.writeTo(sensitiveUnclassifiedObject)
        }
        assertThrows<InformationFlowException> {
            confidentialSubject.writeTo(unclassifiedObject)
        }
    }

    @Test
    fun testIllegalFlowSensitiveUnclassifiedSubject() {
        assertThrows<InformationFlowException> {
            sensitiveUnclassifiedSubject.writeTo(unclassifiedObject)
        }
    }
}