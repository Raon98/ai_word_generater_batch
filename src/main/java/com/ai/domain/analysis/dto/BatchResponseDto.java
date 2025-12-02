package com.ai.domain.analysis.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BatchResponseDto {
    private String fileName;
    private String outputFilePath;
    private String startTime;
    private String endTime;
    private String durationMinutes;
}
