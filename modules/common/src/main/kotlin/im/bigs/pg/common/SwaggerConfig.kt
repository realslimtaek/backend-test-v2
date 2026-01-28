package im.bigs.pg.common

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        // Swagger Title, Version 설정
        .info(Info().title("빅스페이먼츠 과제전형").version("1.0.0"))
        // 서버 URL 설정
        .addServersItem(Server().url("/bix"))
}
