package com.ai.domain.analysis.prompt;

import com.ai.domain.analysis.dto.AnalysisRequestDto;
import org.springframework.stereotype.Component;

@Component
public class JpAnalysisPrompt {

    private static final String SYSTEM_ROLE = """
            You are a veteran Japanese language instructor with over 10 years of teaching experience.
            Your task is to explain Japanese vocabulary clearly to Korean students.
            You must provide the output ONLY in valid JSON format.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Please analyze the following Japanese word for a vocabulary study card.
            
            [Input Data]
            - Expression (Word): %s
            - Reading (Furigana): %s
            - Original Meaning (Eng): %s
            
            [Requirements]
            1. 'meaning': Translate the word's meaning into natural Korean.
            2. 'part_of_speech': Identify the part of speech in Korean (e.g., 명사, 동사, 형용사).
            3. 'example_jp': Create a natural, intermediate-level Japanese example sentence using this word.
            4. 'example_kr': Translate the example sentence into natural Korean.
            
            [Output JSON Schema]
            {
              "expression": "%s",
              "reading": "%s",
              "meaning": "한국어 뜻",
              "part_of_speech": "품사 (한국어)",
              "example_jp": "일본어 예문",
              "example_kr": "예문 한국어 해석"
            }
            """;

    public String getSystemMessage() {
        return SYSTEM_ROLE;
    }

    public String generateUserPrompt(AnalysisRequestDto dto) {
        return String.format(USER_PROMPT_TEMPLATE,
                dto.getExpression(),
                dto.getReading(),
                dto.getOriginalMeaning(),
                dto.getExpression(),
                dto.getReading()
        );
    }
}