package com.ai.domain.analysis.controller;

import com.ai.domain.analysis.dto.BatchResponseDto;
import com.ai.domain.analysis.service.AnalysisBatchService;
import com.ai.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class AnalysisBatchController {

    private final AnalysisBatchService analysisBatchService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<BatchResponseDto>> runBatch(@RequestParam("file") MultipartFile file) {
        try {
            BatchResponseDto resultDto = analysisBatchService.runBatchJob(file);

            return ResponseEntity.ok(ApiResponse.success("배치 분석이 성공적으로 완료되었습니다.", resultDto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("잘못된 요청: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("서버 내부 오류: " + e.getMessage()));
        }
    }
}