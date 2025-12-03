package com.ai.domain.analysis.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class BatchRequestDto {
    private MultipartFile file;
    private String fileType;
}
