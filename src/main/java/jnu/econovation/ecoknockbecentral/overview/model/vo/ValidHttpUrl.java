package jnu.econovation.ecoknockbecentral.overview.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataMeaningException;
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataSyntaxException;

import java.net.URI;

@Embeddable
public record ValidHttpUrl(String value) {
    private static final int MAX_URL_LENGTH = 2048;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ValidHttpUrl {
        validateHttpUrl(value);
    }

    @JsonValue
    @Override
    public String value() {
        return value;
    }

    private static void validateHttpUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new BadDataSyntaxException("url은 필수입니다.");
        }

        if (value.length() > MAX_URL_LENGTH) {
            throw new BadDataMeaningException("url은 " + MAX_URL_LENGTH + "자를 초과할 수 없습니다.");
        }

        URI uri = createUri(value);

        if (!isHttpUrl(uri)) {
            throw new BadDataSyntaxException("올바르지 않은 url 입니다.");
        }
    }

    private static URI createUri(String value) {
        try {
            return URI.create(value);
        } catch (IllegalArgumentException e) {
            throw new BadDataSyntaxException("올바르지 않은 url 입니다.");
        }
    }

    private static boolean isHttpUrl(URI uri) {
        return ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                && uri.getHost() != null
                && !uri.getHost().isBlank();
    }
}
