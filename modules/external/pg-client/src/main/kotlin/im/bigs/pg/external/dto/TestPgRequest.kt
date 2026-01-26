package im.bigs.pg.external.dto

import im.bigs.pg.application.pg.port.out.TestPgReq
import java.math.BigDecimal

data class TestPgRequest(
    // 아래는 testPayment용 입니다.
    override val birthDate: String? = null,
    override val cardNumber: String? = null,
    override val expiry: String? = null,
    override val password: String? = null,
    override val amount: BigDecimal,
) : TestPgReq
