package com.ai.domain.analysis.batch;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JobResultListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        List<AnalysisRequestDto> failed = FailedWordsCollector.getFailedList();

        if (failed.isEmpty()) {
            log.info("실패 단어 없음 → failed_words.json 생성 안 함");
            return;
        }

        String uuid = UUID.randomUUID().toString();
        String baseDir = System.getProperty("user.dir") + "/result";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("result 폴더가 없어 새로 생성했습니다: {}", baseDir);
            } else {
                log.error("result 폴더 생성 실패: {}", baseDir);
            }
        }

        log.info("실패 단어 {}개 저장 시작...", failed.size());

        try (PrintWriter out = new PrintWriter(baseDir +"/"+uuid + "/failed_words.json")) {
            out.println("[");
            for (int i = 0; i < failed.size(); i++) {
                AnalysisRequestDto f = failed.get(i);
                out.printf("  {\"expression\": \"%s\", \"reading\": \"%s\"}%s\n",
                        f.getExpression(),
                        f.getReading(),
                        i < failed.size() - 1 ? "," : ""
                );
            }
            out.println("]");
        } catch (Exception e) {
            log.error("failed_words.json 저장 실패", e);
        }

        log.info("실패 단어 저장 완료! → failed_words.json");
    }
}
