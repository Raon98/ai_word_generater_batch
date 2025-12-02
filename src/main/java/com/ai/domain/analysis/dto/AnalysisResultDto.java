package com.ai.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AnalysisResultDto {
    private final String expression;
    private final String reading;

    @JsonProperty("meaning")
    private final String meaningKr;

    @JsonProperty("part_of_speech")
    private final String partOfSpeech;

    @JsonProperty("example_jp")
    private final String exampleJp;

    @JsonProperty("example_kr")
    private final String exampleKr;

    @Builder
    public AnalysisResultDto(String expression, String reading, String meaningKr,
                             String partOfSpeech, String exampleJp, String exampleKr) {
        this.expression = expression;
        this.reading = reading;
        this.meaningKr = meaningKr;
        this.partOfSpeech = partOfSpeech;
        this.exampleJp = exampleJp;
        this.exampleKr = exampleKr;
    }
}
