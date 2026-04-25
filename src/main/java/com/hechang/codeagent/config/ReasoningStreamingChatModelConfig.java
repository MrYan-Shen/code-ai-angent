package com.hechang.codeagent.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
@Slf4j
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    private String modelName;

    private Integer maxTokens;

    /**
     * 推理流式模型（用于 Vue 项目生成，带工具调用）
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 使用配置文件中的模型名称和参数
        String model = (modelName != null && !modelName.isEmpty()) ? modelName : "qwen-max";
        int tokens = (maxTokens != null && maxTokens > 0) ? maxTokens : 8192;
        
        log.info("初始化推理流式模型: {}, maxTokens: {}", model, tokens);
        
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .maxTokens(tokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
