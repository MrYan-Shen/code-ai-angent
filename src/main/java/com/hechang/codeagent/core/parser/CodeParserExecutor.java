package com.hechang.codeagent.core.parser;

import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器
 * 根据代码生成类型执行相应的解析逻辑
 */
public class CodeParserExecutor {

    private static final HtmlCoderParser htmlCoderParser = new HtmlCoderParser();

    private static final MultiFileCodeParser multiFileCodeParser= new MultiFileCodeParser();

    /**
     * 执行解析器
     * @param userMessage 用户输入
     * @param codeGenType 生成模式
     * @return 解析结果
     */
    public static Object executeParser(String userMessage, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCoderParser.parseCode(userMessage);
            case MULTI_FILE -> multiFileCodeParser.parseCode(userMessage);
            default -> throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,"不支持的生成类型:" + codeGenType
            );
        };
    }
}
