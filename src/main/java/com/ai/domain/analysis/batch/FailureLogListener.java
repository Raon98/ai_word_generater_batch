package com.ai.domain.analysis.batch;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Slf4j
@Component
public class FailureLogListener implements SkipListener<AnalysisRequestDto, Object> {
    private static final String ERROR_FILE_PATH = "error_words.txt";

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("읽기 실패 (Format 에러 등): {}", t.getMessage());
    }

    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.error("쓰기 실패: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(AnalysisRequestDto item, Throwable t) {
        log.warn("GPT 호출 실패 - 단어: {}, 에러: {}", item.getExpression(), t.getMessage());
        writeToFile(item, t.getMessage());
    }

    private synchronized void writeToFile(AnalysisRequestDto item, String errorMessage) {
        try (FileWriter fw = new FileWriter(ERROR_FILE_PATH, true); // true = 이어쓰기 모드
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            String logLine = String.format("[%s] %s (%s) - Cause: %s",
                    LocalDateTime.now(),
                    item.getExpression(),
                    item.getReading(),
                    errorMessage);

            out.println(logLine);

        } catch (IOException e) {
            log.error("에러 로그 파일 기록 실패", e);
        }
    }

}
