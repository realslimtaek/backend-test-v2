package im.bigs.pg.api.payment.dto.validation

import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CreatePaymentRequestValidator : ConstraintValidator<ValidPaymentRequest, CreatePaymentRequest> {

    override fun isValid(value: CreatePaymentRequest?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true // DTO 자체가 null인 경우는 @NotNull 등으로 처리
        }

        context.disableDefaultConstraintViolation()

        return when (value.partnerId) {
            1L -> validatePartner1(value, context)
            2L -> validatePartner2(value, context)
            else -> true
        }
    }

    private fun validatePartner1(value: CreatePaymentRequest, context: ConstraintValidatorContext): Boolean {
        var isValid = true
        if (value.cardBin == null) {
            context.addConstraintViolation("cardBin", "cardBin은 필수 값입니다.")
            isValid = false
        }
        if (value.cardLast4 == null) {
            context.addConstraintViolation("cardLast4", "cardLast4는 필수 값입니다.")
            isValid = false
        }
        if (value.productName == null) {
            context.addConstraintViolation("productName", "productName은 필수 값입니다.")
            isValid = false
        }
        return isValid
    }

    private fun validatePartner2(value: CreatePaymentRequest, context: ConstraintValidatorContext): Boolean {
        var isValid = true
        if (value.birthDate == null) {
            context.addConstraintViolation("birthDate", "birthDate는 필수 값입니다.")
            isValid = false
        } else if (!value.birthDate.matches("^[0-9]{8}$".toRegex())) {
            context.addConstraintViolation("birthDate", "생년월일 형식(YYYYMMDD)이 올바르지 않습니다.")
            isValid = false
        }

        if (value.cardNumber == null) {
            context.addConstraintViolation("cardNumber", "cardNumber는 필수 값입니다.")
            isValid = false
        }

        if (value.expiry == null) {
            context.addConstraintViolation("expiry", "expiry는 필수 값입니다.")
            isValid = false
        } else if (!value.expiry.matches("^(0[1-9]|1[0-2])([0-9]{2})$".toRegex())) {
            context.addConstraintViolation("expiry", "만료일 형식(MMYY)이 올바르지 않습니다.")
            isValid = false
        }

        if (value.password == null) {
            context.addConstraintViolation("password", "password는 필수 값입니다.")
            isValid = false
        } else if (!value.password.matches("^[0-9]{2}$".toRegex())) {
            context.addConstraintViolation("password", "비밀번호 형식(앞 2자리)이 올바르지 않습니다.")
            isValid = false
        }
        return isValid
    }

    private fun ConstraintValidatorContext.addConstraintViolation(field: String, message: String) {
        this.buildConstraintViolationWithTemplate(message)
            .addPropertyNode(field)
            .addConstraintViolation()
    }
}
