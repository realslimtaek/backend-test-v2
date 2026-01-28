package im.bigs.pg.api.payment.adapter

import im.bigs.pg.application.payment.port.out.PaymentQuery
import im.bigs.pg.application.payment.port.out.PaymentSummaryFilter
import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.infra.persistence.payment.repository.PaymentJpaRepository
import im.bigs.pg.infra.persistence.payment.adapter.PaymentPersistenceAdapter // Original adapter import
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
class PaymentPersistenceAdapterTest {

    @Autowired
    private lateinit var adapter: PaymentPersistenceAdapter

    @Autowired
    private lateinit var repository: PaymentJpaRepository

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    private fun createTestPayment(
        partnerId: Long,
        status: PaymentStatus = PaymentStatus.APPROVED,
        amount: BigDecimal = BigDecimal.ZERO,
        netAmount: BigDecimal = BigDecimal.ZERO,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): Payment {
        return Payment(
            id = null,
            partnerId = partnerId,
            amount = amount,
            appliedFeeRate = BigDecimal("0.03"),
            feeAmount = amount.multiply(BigDecimal("0.03")),
            netAmount = netAmount,
            cardBin = "123456",
            cardLast4 = "7890",
            approvalCode = "test-approval-code",
            approvedAt = LocalDateTime.now(),
            status = status,
            createdAt = createdAt,
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `save는 payment 도메인 객체를 받아 엔티티로 변환하여 저장한다`() {
        // given
        val payment = createTestPayment(1L, amount = BigDecimal("1000"))

        // when
        val savedPayment = adapter.save(payment)

        // then
        assertThat(savedPayment.id).isNotNull()
        val foundEntity = repository.findById(savedPayment.id!!)
        assertThat(foundEntity).isPresent
        assertThat(foundEntity.get().id).isEqualTo(savedPayment.id)
        assertThat(foundEntity.get().amount).isEqualByComparingTo(payment.amount)
    }

    @Test
    fun `findBy는 쿼리에 맞는 결과를 커서 기반 페이지로 반환한다`() {
        // given
        val partnerId = 1L
        val now = LocalDateTime.now()
        // 생성 시간 순서대로 저장 (인덱스 0이 가장 최신)
        val payments = (0..4).map {
            adapter.save(createTestPayment(partnerId, createdAt = now.minusHours(it.toLong())))
        }

        // when: 첫 페이지 조회
        val query1 = PaymentQuery(partnerId = partnerId, limit = 2)
        val page1 = adapter.findBy(query1)

        // then: 첫 페이지 검증
        assertThat(page1.items).hasSize(2)
        assertThat(page1.hasNext).isTrue()
        assertThat(page1.items.map { it.id }).containsExactly(payments[0].id, payments[1].id)

        // when: 두 번째 페이지 조회
        val query2 = PaymentQuery(
            partnerId = partnerId,
            limit = 2,
            cursorId = page1.nextCursorId,
            cursorCreatedAt = page1.nextCursorCreatedAt
        )
        val page2 = adapter.findBy(query2)

        // then: 두 번째 페이지 검증
        assertThat(page2.items).hasSize(2)
        assertThat(page2.hasNext).isTrue()
        assertThat(page2.items.map { it.id }).containsExactly(payments[2].id, payments[3].id)
    }

    @Test
    fun `summary는 필터에 맞는 결제 건들의 요약 정보를 반환한다`() {
        // given
        val partnerId = 1L
        adapter.save(
            createTestPayment(
                partnerId,
                status = PaymentStatus.APPROVED,
                amount = BigDecimal("1000"),
                netAmount = BigDecimal("970")
            )
        )
        adapter.save(
            createTestPayment(
                partnerId,
                status = PaymentStatus.APPROVED,
                amount = BigDecimal("2000"),
                netAmount = BigDecimal("1940")
            )
        )
        adapter.save(
            createTestPayment( // This should be filtered out
                partnerId,
                status = PaymentStatus.CANCELED,
                amount = BigDecimal("500"),
                netAmount = BigDecimal("500")
            )
        )

        // when
        val filter = PaymentSummaryFilter(partnerId = partnerId, status = PaymentStatus.APPROVED)
        val summary = adapter.summary(filter)

        // then
        assertThat(summary.count).isEqualTo(2)
        assertThat(summary.totalAmount).isEqualByComparingTo(BigDecimal("3000"))
        assertThat(summary.totalNetAmount).isEqualByComparingTo(BigDecimal("2910"))
    }
}
