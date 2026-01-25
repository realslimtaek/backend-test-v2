package im.bigs.pg.api.payment.dto

import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class CreatePaymentRequest(
    val partnerId: Long,
    @field:Min(1)
    val amount: BigDecimal,

    // 아래는 mockpayment용 입니다.
    val cardBin: String? = null,
    val cardLast4: String? = null,
    val productName: String? = null,

    // 아래는 testPayment용 입니다.
    val birthDate: String? = null,
    val cardNumber: String? = null,
    val expiry: String? = null,
    val password: String? = null,

)
