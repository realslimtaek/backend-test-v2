package im.bigs.pg.external.dto

import java.math.BigDecimal

data class TestPgRequest(
    // 아래는 testPayment용 입니다.
    val birthDate: String? = null,
    val cardNumber: String? = null,
    val expiry: String? = null,
    val password: String? = null,
    val amount: BigDecimal,
)
