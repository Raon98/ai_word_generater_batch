package com.ai.domain.analysis.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobProgressListener implements ChunkListener {
    @Override
    public void beforeChunk(ChunkContext chunkContext) {

    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();

        Object totalCountObj = stepExecution.getJobExecution().getJobParameters().getParameters().get("totalCount").getValue();
        long totalCount = (totalCountObj != null) ? Long.parseLong(totalCountObj.toString()) : 0L;
        long writeCount = stepExecution.getWriteCount();

        if (totalCount > 0) {
            double percent = (double) writeCount / totalCount * 100.0;
            String message = String.format(">>>> [진행률: %.1f%%] (%d / %d)", percent, writeCount, totalCount);

            log.info(message);
        } else {
            log.info(">>>> [처리중] {}건 완료 (총 개수 파악 불가)", writeCount);
        }
    }

    @Override
    public void afterChunkError(ChunkContext chunkContext) {
    }
}
