package im.bigs.pg.application.payment.port.`in`

import im.bigs.pg.application.pg.port.out.PgApproveReq
import java.math.BigDecimal

/**
 * 결제 생성에 필요한 최소 입력.
 *
 * @property partnerId 제휴사 식별자
 * @property amount 결제 금액(정수 금액 권장)
 * @property cardBin 카드 BIN(없을 수 있음)
 * @property cardLast4 카드 마지막 4자리(없을 수 있음)
 * @property productName 상품명(없을 수 있음)
 */
data class PaymentCommand(
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
