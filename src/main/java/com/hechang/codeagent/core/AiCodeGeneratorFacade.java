package com.hechang.codeagent.core;

import com.hechang.codeagent.ai.AiCodeGeneratorService;
import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * AI代码生成器外观类，组合生成和保存功能
 */
@Service
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    public File generateCode(String userMessage, CodeGenTypeEnum codeGenType) {
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"请选择生成模式");
        }
        // 根据 类型 返回生成的代码
        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,errorMessage);
            }
        };
    }

    /**
     * 生成并保存Html代码
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateCode(userMessage);

        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);

        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }
}
