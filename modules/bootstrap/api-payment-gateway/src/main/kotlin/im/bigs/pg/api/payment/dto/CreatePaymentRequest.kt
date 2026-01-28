package im.bigs.pg.api.payment.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreatePaymentRequest(
    @field:Schema(description = "결제할 금액", example = "1000", required = true, minimum = "1")
    @field:Min(value = 1, message = "금액은 1원 이상이어야 합니다.")
    @field:NotNull
    val amount: BigDecimal,

    @field:Valid
    @field:NotNull
    val payload: PaymentPayload
)
