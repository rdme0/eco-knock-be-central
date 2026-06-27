package jnu.econovation.ecoknockbecentral.common.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jnu.econovation.ecoknockbecentral.common.dto.response.CommonResponse;
import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class Rest400Handler {
    private final ObjectMapper mapper;

    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            ClientException exception
    ) throws IOException {
        response.setStatus(exception.getErrorCode().getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        log.warn(
                "잘못된 요청 -> {}, URI - {}",
                exception.getMessage(),
                request.getRequestURI()
        );

        mapper.writeValue(
                response.getWriter(),
                CommonResponse.failure(exception.getErrorCode(), exception.getMessage())
        );
    }
}
