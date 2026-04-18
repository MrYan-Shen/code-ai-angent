package com.hechang.codeagent.ai;


import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成器Service
 */
public interface AiCodeGeneratorService {

    /**
     * 生成代码
     * @param prompt 提示
     * @return 生成的代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateCode(String prompt);

    /**
     * 生成多文件代码
     * @param prompt 提示
     * @return 生成的代码结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String prompt);
}
