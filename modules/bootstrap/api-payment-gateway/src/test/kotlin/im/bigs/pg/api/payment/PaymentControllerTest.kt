package im.bigs.pg.api.payment

import com.fasterxml.jackson.databind.ObjectMapper
import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 각 테스트 후 DB 롤백
class PaymentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    @DisplayName("POST /api/v1/payments - 1번 파트너 결제 생성에 성공하고 200 OK를 반환한다")
    fun `유효한 1번 파트너 결제요청`() {
        // given
        val request = CreatePaymentRequest(
            partnerId = 1,
            amount = BigDecimal("10000"),
            cardBin = "19900101",
            cardLast4 = "1234",
            productName = "삼성전자"
        )

        // when & then
        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { exists() }
            jsonPath("$.status") { value("APPROVED") }
            jsonPath("$.amount") { value(10000) }
        }
    }

    @Test
    @DisplayName("POST /api/v1/payments - 2번 파트너 결제 생성에 성공하고 200 OK를 반환한다")
    fun `유효한 요청`() {
        // given
        val request = CreatePaymentRequest(
            partnerId = 2,
            amount = BigDecimal("10000"),
            birthDate = "19900101",
            cardNumber = "1111-1111-1111-1111",
            expiry = "1225",
            password = "12"
        )

        // when & then
        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { exists() }
            jsonPath("$.status") { value("APPROVED") }
            jsonPath("$.amount") { value(10000) }
        }
    }

    @Test
    @DisplayName("POST /api/v1/payments - 1번 파트너 필수 필드 누락 시 400 Bad Request를 반환한다")
    fun `1번 파트너 유효성 검증 실패`() {
        val request = CreatePaymentRequest(
            partnerId = 1,
            amount = BigDecimal("10000")
            // cardBin, cardLast4, productName 누락
        )

        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @DisplayName("POST /api/v1/payments - 유효성 검증 실패 시 400 Bad Request를 반환한다")
    fun `유효성 검증 실패`() {

        val request = CreatePaymentRequest(
            partnerId = 2,
            amount = BigDecimal("10000")
        )

        // when & then
        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @DisplayName("GET /api/v1/payments - 결제 목록 및 통계 조회 성공")
    fun `결제 목록 조회`() {
        // given: 데이터 2건 생성
        val req1 = CreatePaymentRequest(
            partnerId = 1, amount = BigDecimal("1000"),
            cardBin = "123456", cardLast4 = "1111", productName = "A"
        )
        val req2 = CreatePaymentRequest(
            partnerId = 1, amount = BigDecimal("2000"),
            cardBin = "123456", cardLast4 = "2222", productName = "B"
        )

        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req1)
        }.andExpect { status { isOk() } }

        mockMvc.post("/api/v1/payments") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req2)
        }.andExpect { status { isOk() } }

        // when & then: 조회
        mockMvc.get("/api/v1/payments") {
            param("partnerId", "1")
            param("limit", "10")
        }.andExpect {
            status { isOk() }
            // 목록 검증
            jsonPath("$.items") { isArray() }
            jsonPath("$.items.length()") { value(2) } // 2건 조회 확인

            // 통계 검증
            jsonPath("$.summary.count") { value(2) }
            jsonPath("$.summary.totalAmount") { value(3000) } // 1000 + 2000

            // 커서 정보 존재 확인
            jsonPath("$.hasNext") { value(false) }
        }
    }
}
