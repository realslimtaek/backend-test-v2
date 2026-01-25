package im.bigs.pg.application.pg.port.out

import java.math.BigDecimal

/** PG 승인 요청 최소 정보. */
data class PgApproveRequest(
    val partnerId: Long,
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
