package jnu.econovation.ecoknockbecentral.overview.model.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import jnu.econovation.ecoknockbecentral.common.exception.client.BadDataSyntaxException;

import java.util.List;

@Embeddable
public record GridSize(Integer value) {
    private static final List<Integer> GRID_SIZES = List.of(2, 3);

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public GridSize {
        validate(value);
    }

    @JsonValue
    @Override
    public Integer value() {
        return value;
    }

    public static void validate(Integer value) {
        if (!GRID_SIZES.contains(value)) {
            throw new BadDataSyntaxException("그리드 크기는 %s만 가능합니다.".formatted(GRID_SIZES));
        }
    }
}