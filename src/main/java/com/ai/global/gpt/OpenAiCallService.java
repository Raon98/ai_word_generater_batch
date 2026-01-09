package com.ai.global.gpt;

import com.ai.global.config.GptConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiCallService {
    private final GptConfig gptConfig;
    private final ObjectMapper objectMapper;
    private OpenAiService openAiService;

    @PostConstruct
    public void init() {
        log.info(">>>> 적용된 API Key: {}", gptConfig.secretKey());

        this.openAiService = new OpenAiService(gptConfig.secretKey(), Duration.ofSeconds(60));
    }

    public <T> T callGpt(GptRequest request, Class<T> clazz) {
        List<ChatMessage> messages = List.of(
                new ChatMessage("system", request.getSystemMsg()),
                new ChatMessage("user", request.getPrompt())
        );

        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .model(request.getModel() != null ? request.getModel() : "gpt-4o-mini")
                .messages(messages)
                .temperature(0.7)
                .build();

        try {
            String content = openAiService.createChatCompletion(chatRequest)
                    .getChoices().get(0).getMessage().getContent();

            log.debug("GPT Response: {}", content);

            return objectMapper.readValue(content, clazz);
        } catch (Exception e) {
            log.error("GPT 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("GPT processing failed", e);
        }
    }
}
