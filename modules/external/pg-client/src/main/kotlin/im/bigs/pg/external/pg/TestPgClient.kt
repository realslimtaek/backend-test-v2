package im.bigs.pg.external.pg

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.application.pg.port.out.PgFailedResult
import im.bigs.pg.common.Encrypt
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

    private val objectMapper = ObjectMapper()

    private val webClient = WebClient.builder()
        .baseUrl("https://api-test-pg.bigs.im/api/v1/pay")
        .defaultHeader("API_KEY", API_KEY)
        .build()

    override fun supports(partnerId: Long): Boolean = partnerId % 2L == 0L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        return try {
            webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(encryptor.encrypt(objectMapper.writeValueAsString(request)))
                .retrieve()
                .bodyToMono(PgApproveResult::class.java)
                .block() ?: throw RuntimeException("failed")
        } catch (e: WebClientResponseException) {
            when (e.statusCode.value()) {
                401 -> {
                    throw IllegalArgumentException("Authentication failed")
                }

                422 -> {
                    throw IllegalArgumentException(objectMapper.writeValueAsString(e.responseBodyAs()))
                }

                else -> throw IllegalArgumentException("Unexpected error")
            }
        }
    }


    private fun WebClientResponseException.responseBodyAs(): PgFailedResult? =
        this.responseBodyAsByteArray.let { bytes ->
            ObjectMapper().readValue(bytes, PgFailedResult::class.java)
        }
}
