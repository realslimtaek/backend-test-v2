package im.bigs.pg.api.payment.dto

import im.bigs.pg.api.payment.dto.validation.ValidPaymentRequest
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.math.BigDecimal

@ValidPaymentRequest
data class CreatePaymentRequest(
    @field:Schema(description = "제휴사 고유 ID. 어떤 제휴사를 통해 결제할지 명시합니다.", example = "1", required = true)
    @field:Min(value = 1, message = "partnerId는 1 또는 2여야 합니다.")
    @field:Max(value = 2, message = "partnerId는 1 또는 2여야 합니다.")
    val partnerId: Long,

    @field:Schema(description = "결제할 금액", example = "1000", required = true, minimum = "1")
    @field:Min(value = 1, message = "금액은 1원 이상이어야 합니다.")
    val amount: BigDecimal,

    @field:Schema(description = "[1번 제휴사 전용] 카드 BIN 번호", example = "123456")
    val cardBin: String? = null,
    @field:Schema(description = "[1번 제휴사 전용] 카드번호 마지막 4자리", example = "1111")
    val cardLast4: String? = null,
    @field:Schema(description = "[1번 제휴사 전용] 상품명", example = "삼성전자")
    val productName: String? = null,

    @field:Schema(description = "[2번 제휴사 전용] 소유자 생년월일 (YYYYMMDD 형식)", example = "20000101")
    val birthDate: String? = null,
    @field:Schema(description = "[2번 제휴사 전용] 카드 전체 번호", example = "1111-1111-1111-1111")
    val cardNumber: String? = null,
    @field:Schema(description = "[2번 제휴사 전용] 카드 유효기간 (MMYY 형식)", example = "1227")
    val expiry: String? = null,
    @field:Schema(description = "[2번 제휴사 전용] 카드 비밀번호 앞 2자리", example = "12")
    val password: String? = null,
)
