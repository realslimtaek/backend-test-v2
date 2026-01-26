package im.bigs.pg.api.payment.dto.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CreatePaymentRequestValidator::class])
annotation class ValidPaymentRequest(
    val message: String = "결제 요청이 유효하지 않습니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
