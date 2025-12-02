package com.ai.domain.analysis.service;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import com.ai.domain.analysis.dto.AnalysisResultDto;
import com.ai.domain.analysis.prompt.JpAnalysisPrompt;
import com.ai.global.gpt.GptRequest;
import com.ai.global.gpt.OpenAiCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GptAnalysisService {

    private final OpenAiCallService openAiCallService;
    private final JpAnalysisPrompt jpAnalysisPrompt;

    public AnalysisResultDto analyzeWord(AnalysisRequestDto requestDto) {

        String systemMsg = jpAnalysisPrompt.getSystemMessage();
        String userMsg = jpAnalysisPrompt.generateUserPrompt(requestDto);

        GptRequest request = GptRequest.builder()
                .systemMsg(systemMsg)
                .prompt(userMsg)
                .build();

        try {
            return openAiCallService.callGpt(request, AnalysisResultDto.class);

        } catch (Exception e) {
            log.error("GPT 분석 중 오류 발생 - 단어: {}", requestDto.getExpression(), e);

            // 4. 실패 시 fallback (배치가 멈추지 않도록 에러 정보 담아서 리턴)
            return AnalysisResultDto.builder()
                    .expression(requestDto.getExpression())
                    .reading(requestDto.getReading())
                    .meaningKr("분석 실패: " + e.getMessage())
                    .build();
        }
    }
}
