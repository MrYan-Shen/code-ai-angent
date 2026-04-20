package com.hechang.codeagent;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude =  {
        // 排除 embedding 的自动装配
        RedisEmbeddingStoreAutoConfiguration.class
})
@MapperScan("com.hechang.codeagent.mapper")
public class CodeAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAgentApplication.class, args);
    }

}
