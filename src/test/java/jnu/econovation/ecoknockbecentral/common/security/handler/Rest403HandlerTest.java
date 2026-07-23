package jnu.econovation.ecoknockbecentral.common.security.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;

class Rest403HandlerTest {

    @Test
    void returnsCommonResponseWhenAccessIsDenied() throws Exception {
        Rest403Handler handler = new Rest403Handler(new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wallet/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(response.getContentAsString())
                .contains("\"isSuccess\":false")
                .contains("\"message\":\"접근 권한이 없습니다.\"")
                .contains("\"errorCode\":\"SECURITY_403_001\"");
    }
}
