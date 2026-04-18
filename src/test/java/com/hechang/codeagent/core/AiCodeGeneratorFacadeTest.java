package com.hechang.codeagent.core;

import com.hechang.codeagent.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateCodeTest() {
        File file = aiCodeGeneratorFacade.generateCode("做一个聊天室网站,包含发言部分和留言板部分", CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }
}