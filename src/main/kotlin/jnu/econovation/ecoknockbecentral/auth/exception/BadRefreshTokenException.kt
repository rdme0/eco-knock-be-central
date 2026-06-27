package jnu.econovation.ecoknockbecentral.auth.exception

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode

class BadRefreshTokenException : ClientException(ErrorCode.BAD_REFRESH_TOKEN)
