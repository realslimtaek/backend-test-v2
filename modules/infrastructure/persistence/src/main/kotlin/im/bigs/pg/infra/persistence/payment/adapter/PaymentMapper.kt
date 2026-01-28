package im.bigs.pg.infra.persistence.payment.adapter

import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentStatus
import im.bigs.pg.infra.persistence.payment.entity.PaymentEntity
import org.springframework.stereotype.Component
import java.time.ZoneOffset

@Component
object PaymentMapper {

    /** 도메인 → 엔티티 매핑. */
    fun toEntity(domain: Payment) =
        PaymentEntity(
            id = domain.id,
            partnerId = domain.partnerId,
            amount = domain.amount,
            appliedFeeRate = domain.appliedFeeRate,
            feeAmount = domain.feeAmount,
            netAmount = domain.netAmount,
            cardBin = domain.cardBin,
            cardLast4 = domain.cardLast4,
            approvalCode = domain.approvalCode,
            approvedAt = domain.approvedAt.toInstant(ZoneOffset.UTC),
            status = domain.status.name,
            createdAt = domain.createdAt.toInstant(ZoneOffset.UTC),
            updatedAt = domain.updatedAt.toInstant(ZoneOffset.UTC),
        )

    /** 엔티티 → 도메인 매핑. */
    fun toDomain(entity: PaymentEntity) =
        Payment(
            id = entity.id,
            partnerId = entity.partnerId,
            amount = entity.amount,
            appliedFeeRate = entity.appliedFeeRate,
            feeAmount = entity.feeAmount,
            netAmount = entity.netAmount,
            cardBin = entity.cardBin,
            cardLast4 = entity.cardLast4,
            approvalCode = entity.approvalCode,
            approvedAt = java.time.LocalDateTime.ofInstant(entity.approvedAt, ZoneOffset.UTC),
            status = PaymentStatus.valueOf(entity.status),
            createdAt = java.time.LocalDateTime.ofInstant(entity.createdAt, ZoneOffset.UTC),
            updatedAt = java.time.LocalDateTime.ofInstant(entity.updatedAt, ZoneOffset.UTC),
        )
}
