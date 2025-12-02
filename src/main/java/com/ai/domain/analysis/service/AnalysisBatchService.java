package com.ai.domain.analysis.service;

import com.ai.domain.analysis.dto.BatchResponseDto;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisBatchService {
    private final JobLauncher jobLauncher;
    private final Job analysisJob;

    /**
     * 파일을 저장하고 배치를 실행하는 핵심 로직
     */
    public BatchResponseDto runBatchJob(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");

        try {

            String inputFilePath = saveFileToTemp(file);

            long totalCount = countTotalRows(inputFilePath);
            log.info("총 데이터 개수 확인: {}건", totalCount);

            String uuid = UUID.randomUUID().toString();
            String outputFileName = "result_" + uuid + ".json";
            String outputFilePath = System.getProperty("user.dir") + "/result/" + outputFileName;

            JobParameters params = new JobParametersBuilder()
                    .addString("filePath", inputFilePath)
                    .addString("outputPath", outputFilePath)
                    .addString("runId", uuid)
                    .addLong("totalCount", totalCount)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(analysisJob, params);

            return createReport(execution, outputFileName, outputFilePath);

        } catch (Exception e) {
            log.error("배치 실행 실패", e);
            throw new RuntimeException("배치 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private long countTotalRows(String filePath) {
        AtomicLong count = new AtomicLong();
        EasyExcel.read(filePath, new AnalysisEventListener<Object>() {
            @Override public void invoke(Object data, AnalysisContext context) {}
            @Override public void doAfterAllAnalysed(AnalysisContext context) {
                count.set(context.readRowHolder().getRowIndex());
            }
        }).sheet().doRead();
        return count.get();
    }

    private String saveFileToTemp(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String tempPath = System.getProperty("user.dir") + "/temp/" + fileName;

        File dest = new File(tempPath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        log.info("파일 임시 저장 완료: {}", tempPath);
        return tempPath;
    }

    private BatchResponseDto createReport(JobExecution execution, String fileName, String path) {
        LocalDateTime start = execution.getStartTime();
        LocalDateTime end = execution.getEndTime();
        if (end == null) end = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        long durationMillis = Duration.between(start, end).toMillis();

        long minutes = durationMillis / 60000;
        double seconds = (durationMillis % 60000) / 1000.0;
        String formattedDuration = String.format("%d분 %.1f초", minutes, seconds);

        return BatchResponseDto.builder()
                .fileName(fileName)
                .outputFilePath(path)
                .startTime(start.format(formatter))
                .endTime(end.format(formatter))
                .durationMinutes(formattedDuration)
                .build();
    }

}
