package im.bigs.pg.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class Encrypt(
    @Value("\${dv.api_key}")
    private val API_KEY: String,

    @Value("\${dv.iv}")
    private val IV: String,
) {

    private val ALGORITHM = "AES/GCM/NoPadding"
    private val TAG_LENGTH_BIT = 128

    fun encrypt(plainText: String): String {

        val keyBytes = MessageDigest.getInstance("SHA-256")
            .digest(API_KEY.toByteArray(StandardCharsets.UTF_8))
        val secretKey = SecretKeySpec(keyBytes, "AES")

        val ivBytes = Base64.getUrlDecoder().decode(IV)

        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, ivBytes)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

        return Base64.getUrlEncoder().withoutPadding().encodeToString(encryptedBytes)
    }

}
