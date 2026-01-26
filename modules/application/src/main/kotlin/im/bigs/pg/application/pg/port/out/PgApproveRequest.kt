package im.bigs.pg.application.pg.port.out

import java.math.BigDecimal

/** PG 승인 요청 최소 정보. */
data class PgApproveRequest(
    override val partnerId: Long,
    override val amount: BigDecimal,

    // 아래는 mockpayment용 입니다.
    override val cardBin: String? = null,
    override val cardLast4: String? = null,
    override val productName: String? = null,

    // 아래는 testPayment용 입니다.
    override val birthDate: String? = null,
    override val cardNumber: String? = null,
    override val expiry: String? = null,
    override val password: String? = null,
) : PgApproveReq

interface PgApproveReq : MockPgReq, TestPgReq {
    val partnerId: Long
}

interface MockPgReq : CommonPgApproveRequest {
    val cardBin: String?
    val cardLast4: String?
    val productName: String?
}

interface TestPgReq : CommonPgApproveRequest {
    val birthDate: String?
    val cardNumber: String?
    val expiry: String?
    val password: String?
}

interface CommonPgApproveRequest {
    val amount: BigDecimal
}
