package com.ai.domain.analysis.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.FileSystemResource;

import com.ai.domain.analysis.dto.AnalysisRequestDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvItemReader {
     public static ItemStreamReader<AnalysisRequestDto> buildCsvReader(String filePath) {
        return new FlatFileItemReaderBuilder<AnalysisRequestDto>()
                .name("csvItemReader")
                .resource(new FileSystemResource(filePath))
                .delimited()
                .names("expression","reading","originalMeaning","tags")
                .linesToSkip(1) 
                .targetType(AnalysisRequestDto.class)
                .build();
    }
}
