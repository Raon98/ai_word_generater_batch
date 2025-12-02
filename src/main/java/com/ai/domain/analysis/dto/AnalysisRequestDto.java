package com.ai.domain.analysis.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AnalysisRequestDto {
    @ExcelProperty(index = 0)
    private String expression;

    @ExcelProperty(index = 1)
    private String reading;

    @ExcelProperty(index = 2)
    private String originalMeaning;

    @ExcelProperty(index = 3)
    private String tags;

}
