package com.ai.domain.analysis.batch;

import com.ai.domain.analysis.dto.AnalysisRequestDto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FailedWordsCollector {
    private static final List<AnalysisRequestDto> failedList = Collections.synchronizedList(new LinkedList<>());

    public static void add(AnalysisRequestDto item) {
        failedList.add(item);
    }

    public static List<AnalysisRequestDto> getFailedList() {
        return failedList;
    }
}
