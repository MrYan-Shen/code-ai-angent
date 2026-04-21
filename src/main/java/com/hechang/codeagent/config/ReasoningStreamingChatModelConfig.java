package com.hechang.codeagent.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 推理流式模型配置
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {
    private String apiKey;

    private String baseUrl;

    private String modelName;

    /**
     * 创建推理流式模型
     * @return 推理流式模型
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();

    }
}
