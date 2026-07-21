package jnu.econovation.ecoknockbecentral.overview.exception;

import jnu.econovation.ecoknockbecentral.common.exception.client.ClientException;
import jnu.econovation.ecoknockbecentral.common.exception.constants.ErrorCode;

public class OverviewLayoutGridSizeConflictException extends ClientException {

    private final Integer gridSize;

    public OverviewLayoutGridSizeConflictException(Integer gridSize) {
        super(ErrorCode.OVERVIEW_LAYOUT_GRID_SIZE_CONFLICT);
        this.gridSize = gridSize;
    }

    @Override
    public String getMessage() {
        return getErrorCode().getMessage().formatted(gridSize);
    }
}
