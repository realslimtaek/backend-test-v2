package im.bigs.pg.api.payment.dto

import im.bigs.pg.api.payment.dto.validation.ValidPaymentRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.math.BigDecimal

@ValidPaymentRequest
data class CreatePaymentRequest(
    @field:Schema(description = "제휴사 ID | 1 or 2", example = "1", required = true)
    @field:Min(value = 1, message = "partnerId는 1 또는 2여야 합니다.")
    @field:Max(value = 2, message = "partnerId는 1 또는 2여야 합니다.")
    val partnerId: Long,

    @field:Schema(description = "결제금액", example = "1000", required = true, minimum = "1")
    @field:Min(value = 1, message = "금액은 1원 이상이어야 합니다.")
    val amount: BigDecimal,

    // 아래는 mockpayment(partnerId=1)용 입니다.
    @field:Schema(description = "카드bin 정보", example = "123456", required = false)
    val cardBin: String? = null,
    @field:Schema(description = "카드 마지막 4자리", example = "1111", required = false)
    val cardLast4: String? = null,
    @field:Schema(description = "상품명", example = "삼성전자", required = false)
    val productName: String? = null,

    // 아래는 testPayment(partnerId=2)용 입니다.
    @field:Schema(description = "생년월일 | YYYYMMDD", example = "20000101", required = false)
    val birthDate: String? = null,
    @field:Schema(description = "카드번호", example = "1111-1111-1111-1111", required = false)
    val cardNumber: String? = null,
    @field:Schema(description = "만료일 | MMYY", example = "1227", required = false)
    val expiry: String? = null,
    @field:Schema(description = "카드비밀번호 앞 두자리", example = "12", required = false)
    val password: String? = null,
)
