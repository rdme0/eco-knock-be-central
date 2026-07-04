package jnu.econovation.ecoknockbecentral.airquality.dto.rest.response

import jnu.econovation.ecoknockbecentral.airquality.dto.internal.AirQualityViewDTO

data class GetAirQualityResponse(val airQualities: List<AirQualityViewDTO>)