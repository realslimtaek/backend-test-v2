package im.bigs.pg.external.pg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.application.pg.port.out.PgFailedResult
import im.bigs.pg.common.Encrypt
import im.bigs.pg.external.dto.TestPgRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

@Component
class TestPgClient(

    @Value("\${dv.api_key}")
    private val API_KEY: String,

    private val encryptor: Encrypt
) : PgClientOutPort {

    private val mapper = jacksonObjectMapper()

    private val webClient = WebClient.builder()
        .baseUrl("https://api-test-pg.bigs.im/api/v1/pay")
        .defaultHeader("API-KEY", API_KEY)
        .build()

    override fun supports(partnerId: Long): Boolean = partnerId % 2L == 0L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        require(request.birthDate != null && request.cardNumber != null && request.expiry != null && request.password != null) {
            throw IllegalArgumentException("birthDate, cardNumber, expiry, password must not be null")
        }

        val enc = mapper.writeValueAsString(
            TestPgRequest(
                birthDate = request.birthDate,
                cardNumber = request.cardNumber,
                expiry = request.expiry,
                password = request.password,
                amount = request.amount
            )
        ).run(encryptor::encrypt)

        return try {
            webClient.post()
                .uri("/credit-card")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(mapOf("enc" to enc))
                .retrieve()
                .bodyToMono(PgApproveResult::class.java)
                .block() ?: throw RuntimeException("failed")
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                401 -> {
                    throw IllegalArgumentException("Authentication failed")
                }

                422 -> {
                    throw IllegalArgumentException(mapper.writeValueAsString(e.responseBodyAs()))
                }

                else -> throw IllegalArgumentException("Unexpected error")
            }
        }
    }

    private fun WebClientResponseException.responseBodyAs(): PgFailedResult? =
        this.responseBodyAsByteArray.let { bytes ->
            mapper.readValue(bytes, PgFailedResult::class.java)
        }
}
