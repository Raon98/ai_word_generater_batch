package com.ai.domain.analysis.batch;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public class EasyExcelItemReader implements ItemStreamReader<AnalysisRequestDto>, ReadListener<AnalysisRequestDto> {

    private final String filePath;
    private final ConcurrentLinkedQueue<AnalysisRequestDto> queue = new ConcurrentLinkedQueue<>();
    private boolean isRead = false;

    public EasyExcelItemReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        if (filePath == null) {
            log.warn("파일 경로가 null입니다. 빈 Reader로 동작합니다.");
            return;
        }

        if (!isRead) {
            log.info(">>>> 엑셀 파일 읽기 시작: {}", filePath);
            EasyExcel.read(filePath, AnalysisRequestDto.class, this)
                    .sheet()
                    .headRowNumber(1)
                    .doRead();
            isRead = true;
            log.info(">>>> 엑셀 파일 읽기 완료. 데이터 수: {}", queue.size());
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {}

    @Override
    public void close() {}

    @Override
    public AnalysisRequestDto read() {
        return queue.poll();
    }

    @Override
    public void invoke(AnalysisRequestDto data, AnalysisContext context) {
        queue.offer(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("엑셀 읽기 완료. 총 데이터 수: {}", queue.size());
    }
}
