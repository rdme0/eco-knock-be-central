package jnu.econovation.ecoknockbecentral.sso.client

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.sso.config.SSOConfig
import jnu.econovation.ecoknockbecentral.sso.constant.SSOConstant.ACCESS_TOKEN_COOKIE
import jnu.econovation.ecoknockbecentral.sso.dto.response.SSOMeResponse
import jnu.econovation.ecoknockbecentral.sso.exception.BadSSOTokenException
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class SsoAuthClient(
    private val properties: SSOConfig,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val client: RestClient = RestClient.builder()
        .baseUrl(properties.baseUrl)
        .build()

    fun getMe(accessToken: String): SSOMeResponse {
        return try {
            client.get()
                .uri(properties.mePath)
                .header(HttpHeaders.COOKIE, "$ACCESS_TOKEN_COOKIE=$accessToken")
                .retrieve()
                .body<SSOMeResponse>()
                ?: throw InternalServerException(
                    IllegalStateException("SSO me 응답 본문이 비어 있음")
                )

        } catch (e: RestClientResponseException) {
            if (e.statusCode == HttpStatus.UNAUTHORIZED) {
                logger.warn { "올바르지 않은 SSO 토큰 : ${accessToken.take(10)}..." }
                throw BadSSOTokenException()
            }

            throw InternalServerException(e)
        } catch (e: RestClientException) {
            throw InternalServerException(e)
        }
    }
}
