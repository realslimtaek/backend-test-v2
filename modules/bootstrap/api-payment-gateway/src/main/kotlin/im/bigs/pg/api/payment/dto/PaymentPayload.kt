package im.bigs.pg.api.payment.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MockPayload::class, name = "mock"),
    JsonSubTypes.Type(value = TestPayload::class, name = "test")
)
@Schema(
    description = "제휴사별 요청 데이터. 'type' 필드에 'mock' 또는 'test'를 명시해야 합니다.",
    discriminatorProperty = "type",
    discriminatorMapping = [
        io.swagger.v3.oas.annotations.media.DiscriminatorMapping(value = "mock", schema = MockPayload::class),
        io.swagger.v3.oas.annotations.media.DiscriminatorMapping(value = "test", schema = TestPayload::class)
    ]
)
sealed class PaymentPayload {
    abstract val type: String
    abstract fun toCommand(amount: BigDecimal): PaymentCommand
}

data class MockPayload(
    @field:Schema(description = "카드 BIN 번호", example = "123456", required = true)
    @field:NotBlank
    val cardBin: String,

    @field:Schema(description = "카드번호 마지막 4자리", example = "1111", required = true)
    @field:NotBlank
    val cardLast4: String,

    @field:Schema(description = "상품명", example = "삼성전자", required = true)
    @field:NotBlank
    val productName: String,

    override val type: String = "mock"
) : PaymentPayload() {
    override fun toCommand(amount: BigDecimal) = PaymentCommand(
        partnerId = 1L, // partnerId 제공
        amount = amount,
        cardBin = this.cardBin,
        cardLast4 = this.cardLast4,
        productName = this.productName
    )
}

data class TestPayload(
    @field:Schema(description = "소유자 생년월일 (YYYYMMDD 형식)", example = "20000101", required = true)
    @field:NotBlank
    val birthDate: String,

    @field:Schema(description = "카드 전체 번호", example = "1111-1111-1111-1111", required = true)
    @field:NotBlank
    val cardNumber: String,

    @field:Schema(description = "카드 유효기간 (MMYY 형식)", example = "1227", required = true)
    @field:NotBlank
    val expiry: String,

    @field:Schema(description = "카드 비밀번호 앞 2자리", example = "12", required = true)
    @field:NotBlank
    val password: String,

    override val type: String = "test"
) : PaymentPayload() {
    override fun toCommand(amount: BigDecimal) = PaymentCommand(
        partnerId = 2L, // partnerId 제공
        amount = amount,
        birthDate = this.birthDate,
        cardNumber = this.cardNumber,
        expiry = this.expiry,
        password = this.password
    )
}
