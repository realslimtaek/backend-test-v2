package im.bigs.pg.application.payment.service

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.application.partner.port.out.PartnerOutPort
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.out.PaymentOutPort
import im.bigs.pg.application.pg.port.out.CommonPgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.partner.FeePolicy
import im.bigs.pg.domain.partner.Partner
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals

class PaymentServiceTest {

    private val partnerRepo = mockk<PartnerOutPort>()
    private val feeRepo = mockk<FeePolicyOutPort>()
    private val paymentRepo = mockk<PaymentOutPort>()
    // 제네릭 타입을 CommonPgApproveRequest로 명시하여 모킹
    private val pgClient = mockk<PgClientOutPort<CommonPgApproveRequest>>()

    // PaymentService는 List<PgClientOutPort<*>>를 받으므로 캐스팅해서 주입
    @Suppress("UNCHECKED_CAST")
    private val service = PaymentService(partnerRepo, feeRepo, paymentRepo, listOf(pgClient as PgClientOutPort<*>))

    @Test
    @DisplayName("결제 시 수수료 정책을 적용하고 저장해야 한다")
    fun `결제 시 수수료 정책을 적용하고 저장해야 한다`() {
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test", true)

        every { pgClient.supports(1L) } returns true

        every { feeRepo.findEffectivePolicy(1L, any()) } returns FeePolicy(
            id = 10L, partnerId = 1L, effectiveFrom = LocalDateTime.ofInstant(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC),
            percentage = BigDecimal("0.0300"), fixedFee = BigDecimal("100")
        )
        val savedSlot = slot<Payment>()
        every { paymentRepo.save(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 99L) }
        every { pgClient.approve(any()) } returns PgApproveResult(
            "APPROVAL-123",
            LocalDateTime.of(2024, 1, 1, 0, 0, 0),
            PaymentStatus.APPROVED
        )
        val cmd = PaymentCommand(
            partnerId = 1L,
            amount = BigDecimal("10000"),
            cardLast4 = "4242",

            birthDate = "19990101",
            cardNumber = "1234-1234-1234-4242",
            expiry = "1225",
            password = "00"
        )
        val res = service.pay(cmd)

        assertEquals(99L, res.id)
        assertEquals(BigDecimal("400"), res.feeAmount)
        assertEquals(BigDecimal("9600"), res.netAmount)
        assertEquals(PaymentStatus.APPROVED, res.status)
    }

    @Test
    @DisplayName("파트너가 존재하지 않으면 예외가 발생해야 한다")
    fun `파트너가 존재하지 않으면 예외가 발생해야 한다`() {
        every { partnerRepo.findById(999L) } returns null

        val cmd = PaymentCommand(partnerId = 999L, amount = BigDecimal("10000"), cardLast4 = "1234")

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.pay(cmd)
        }
        Assertions.assertEquals("Partner not found: 999", exception.message)
    }

    @Test
    @DisplayName("비활성화된 파트너는 결제를 진행할 수 없다")
    fun `비활성화된 파트너는 결제를 진행할 수 없다`() {
        every { partnerRepo.findById(1L) } returns Partner(1L, "INACTIVE", "Inactive Partner", false)

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "1234")

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.pay(cmd)
        }
        Assertions.assertEquals("Partner is inactive: 1", exception.message)
    }

    @Test
    @DisplayName("지원하는 PG 클라이언트가 없으면 예외가 발생해야 한다")
    fun `지원하는 PG 클라이언트가 없으면 예외가 발생해야 한다`() {
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test Partner", true)
        every { pgClient.supports(1L) } returns false

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "1234")

        val exception = assertThrows(IllegalStateException::class.java) {
            service.pay(cmd)
        }
        Assertions.assertEquals("No PG client for partner 1", exception.message)
    }

    @Test
    @DisplayName("수수료 정책이 없으면 예외가 발생해야 한다")
    fun `수수료 정책이 없으면 예외가 발생해야 한다`() {
        every { partnerRepo.findById(1L) } returns Partner(1L, "TEST", "Test Partner", true)
        every { pgClient.supports(1L) } returns true
        every { pgClient.approve(any()) } returns PgApproveResult("CODE", LocalDateTime.now(), PaymentStatus.APPROVED)
        every { feeRepo.findEffectivePolicy(1L, any()) } returns null

        val cmd = PaymentCommand(partnerId = 1L, amount = BigDecimal("10000"), cardLast4 = "1234")

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.pay(cmd)
        }
        Assertions.assertEquals("Fee policy not found for partner 1", exception.message)
    }

    @Test
    @DisplayName("PG 승인 요청 시 올바른 정보가 전달되어야 한다")
    fun `PG 승인 요청 시 올바른 정보가 전달되어야 한다`() {
        val partnerId = 1L
        val amount = BigDecimal("50000")
        val cardLast4 = "5678"
        val birthDate = "19900101"
        val cardNumber = "1234-1234-1234-5678"

        every { partnerRepo.findById(partnerId) } returns Partner(partnerId, "TEST", "Test Partner", true)
        every { pgClient.supports(partnerId) } returns true

        // 캡처할 슬롯의 타입을 CommonPgApproveRequest로 변경 (PgApproveRequest는 이를 구현함)
        val pgRequestSlot = slot<CommonPgApproveRequest>()
        every { pgClient.approve(capture(pgRequestSlot)) } returns PgApproveResult("CODE", LocalDateTime.now(), PaymentStatus.APPROVED)

        every { feeRepo.findEffectivePolicy(partnerId, any()) } returns FeePolicy(
            10L, partnerId, LocalDateTime.now(), BigDecimal("0.01"), BigDecimal.ZERO
        )
        every { paymentRepo.save(any()) } answers { firstArg() }

        val cmd = PaymentCommand(
            partnerId = partnerId,
            amount = amount,
            cardLast4 = cardLast4,
            birthDate = birthDate,
            cardNumber = cardNumber,
            expiry = "1225",
            password = "00"
        )

        service.pay(cmd)

        // 캡처된 객체를 PgApproveRequest로 캐스팅하여 검증 (실제 서비스에서 PgApproveRequest를 생성해서 넘기므로)
        val capturedRequest = pgRequestSlot.captured as PgApproveRequest
        Assertions.assertEquals(partnerId, capturedRequest.partnerId)
        Assertions.assertEquals(amount, capturedRequest.amount)
        Assertions.assertEquals(birthDate, capturedRequest.birthDate)
        Assertions.assertEquals(cardNumber, capturedRequest.cardNumber)
        Assertions.assertEquals("1225", capturedRequest.expiry)
        Assertions.assertEquals("00", capturedRequest.password)
    }
}
