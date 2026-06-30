package jnu.econovation.ecoknockbecentral.airquality.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jnu.econovation.ecoknockbecentral.airquality.service.AirQualitySseService
import jnu.econovation.ecoknockbecentral.common.openapi.OpenApiConstants.SSE_RESPONSE
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/air-quality")
@Tag(name = "Air Quality", description = "공기질 시계열 API")
class AirQualitySseController(
    private val airQualitySseService: AirQualitySseService,
) {
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @Operation(
        summary = "공기질 SSE 스트림 연결",
        description = "실시간 공기질 이벤트를 text/event-stream으로 구독합니다. 연결 직후 connected 이벤트와 ok 데이터를 보냅니다.",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = SSE_RESPONSE,
                content = [
                    Content(
                        mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
                        examples = [ExampleObject(value = "event: connected\ndata: ok\n\n")]
                    )
                ]
            )
        ]
    )
    fun stream(): SseEmitter {
        return airQualitySseService.subscribe()
    }
}
