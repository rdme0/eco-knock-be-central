package jnu.econovation.ecoknockbecentral.common.openapi

import io.swagger.v3.oas.annotations.Hidden
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse.success
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/admin/api-docs-access")
@PreAuthorize("hasRole('ADMIN')")
class AdminApiDocAccessController(
    private val apiDocAccessService: ApiDocAccessService,
) {
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccess(): CommonResponse<ApiDocAccessResponse> {
        return success(ApiDocAccessResponse(enabled = apiDocAccessService.isEnabled()))
    }

    @PutMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun updateAccess(
        @RequestBody request: UpdateApiDocAccessRequest,
    ): CommonResponse<ApiDocAccessResponse> {
        return success(apiDocAccessService.update(request.enabled))
    }
}
