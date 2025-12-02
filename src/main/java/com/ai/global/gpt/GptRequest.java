package com.ai.global.gpt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GptRequest {
    public String systemMsg;
    public String prompt;
    public String model;
}
