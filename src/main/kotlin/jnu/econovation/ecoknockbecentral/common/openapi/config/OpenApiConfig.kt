package jnu.econovation.ecoknockbecentral.common.openapi.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.examples.Example
import jnu.econovation.ecoknockbecentral.auth.constant.AuthConstant.ACCESS_TOKEN
import jnu.econovation.ecoknockbecentral.common.security.constant.AdminAuthConstant.ADMIN_MASTER_TOKEN
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode
import jnu.econovation.ecoknockbecentral.common.openapi.constant.OpenApiConstants
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = OpenApiConstants.ACCESS_TOKEN_SECURITY_SCHEME_NAME,
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.COOKIE,
    paramName = ACCESS_TOKEN,
    description = "서비스 access token 쿠키",
)
@SecurityScheme(
    name = OpenApiConstants.ADMIN_MASTER_TOKEN_SECURITY_SCHEME_NAME,
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.COOKIE,
    paramName = ADMIN_MASTER_TOKEN,
    description = "관리자 마스터 인증 token 쿠키",
)
@OpenAPIDefinition(
    info = Info(
        title = "eco-knock-be-central API",
        version = "0.0.1-SNAPSHOT",
        description = "eco-knock-be-central REST API documentation",
    )
)
class OpenApiConfig {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .findAndRegisterModules()
        .setDefaultPropertyInclusion(
            JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)
        )

    @Bean
    fun commonResponseExamples(): OpenApiCustomizer {
        return OpenApiCustomizer { openApi ->
            val components = openApi.components ?: Components().also { openApi.components = it }

            components.addExamples(OpenApiConstants.EMPTY_SUCCESS_EXAMPLE_NAME, example(CommonResponse.emptySuccess()))
            components.addExamples(
                OpenApiConstants.BAD_DATA_SYNTAX_EXAMPLE_NAME,
                example(ErrorCode.BAD_DATA_SYNTAX, "올바르지 않은 url 입니다.")
            )
            components.addExamples(
                OpenApiConstants.BAD_DATA_MEANING_EXAMPLE_NAME,
                example(ErrorCode.BAD_DATA_MEANING, "sortOrder는 중복될 수 없습니다.")
            )
            components.addExamples(
                OpenApiConstants.INVALID_REDIRECT_URI_EXAMPLE_NAME,
                example(ErrorCode.INVALID_REDIRECT_URI)
            )
            components.addExamples(OpenApiConstants.BAD_SSO_TOKEN_EXAMPLE_NAME, example(ErrorCode.BAD_SSO_TOKEN))
            components.addExamples(
                OpenApiConstants.BAD_REFRESH_TOKEN_EXAMPLE_NAME,
                example(ErrorCode.BAD_REFRESH_TOKEN)
            )
            components.addExamples(
                OpenApiConstants.GUEST_LOGIN_RATE_LIMIT_EXCEEDED_EXAMPLE_NAME,
                example(ErrorCode.GUEST_LOGIN_RATE_LIMIT_EXCEEDED)
            )
            components.addExamples(OpenApiConstants.UNAUTHORIZED_EXAMPLE_NAME, example(ErrorCode.UNAUTHORIZED))
            components.addExamples(
                OpenApiConstants.AIR_QUALITY_BAD_REQUEST_EXAMPLE_NAME,
                example(ErrorCode.BAD_AIR_QUALITY_RESOLUTION)
            )
            components.addExamples(
                OpenApiConstants.AIR_QUALITY_HISTORY_LIMIT_EXAMPLE_NAME,
                example(ErrorCode.BAD_AIR_QUALITY_HISTORY_LIMIT)
            )
            components.addExamples(
                OpenApiConstants.INTERNAL_SERVER_ERROR_EXAMPLE_NAME,
                example(ErrorCode.INTERNAL_SERVER_ERROR)
            )
        }
    }

    private fun example(errorCode: ErrorCode): Example {
        return example(CommonResponse.failure(errorCode))
    }

    private fun example(errorCode: ErrorCode, message: String): Example {
        return example(CommonResponse.failure(errorCode, message))
    }

    private fun example(response: CommonResponse<Void>): Example {
        val value = objectMapper.readTree(objectMapper.writeValueAsString(response))
        return Example().value(value)
    }
}
