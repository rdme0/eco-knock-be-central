package jnu.econovation.ecoknockbecentral.sso.exception

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode

class BadSSOTokenException : ClientException(ErrorCode.BAD_SSO_TOKEN)