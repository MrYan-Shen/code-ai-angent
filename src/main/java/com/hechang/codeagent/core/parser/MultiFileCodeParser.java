package com.hechang.codeagent.core.parser;

import com.hechang.codeagent.ai.model.MultiFileCodeResult;



/**
 * 多文件代码解析器（HTML + CSS + JS）
 *
 * @author chang
 */
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {

    /**
     * 解析多文件代码
     * @param codeContent 代码内容
     * @return 解析后的多文件代码结果
     */
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        // 从完整字符串中提取多文件代码
        return com.hechang.codeagent.core.CodeParser.parseMultiFileCode(codeContent);
    }


}