package com.hechang.codeagent.ai;

import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试 html 是否可用生成
 */
@SpringBootTest
class AiCodeGeneratorServiceTest {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateCode() {
        HtmlCodeResult result = aiCodeGeneratorService.generateCode("做个类似淘宝秒杀的小工具");
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        String prompt = "做一个网页,包含发言部分和留言板部分";
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(prompt);
        Assertions.assertNotNull(result);
    
    }
}