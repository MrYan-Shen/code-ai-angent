package com.hechang.codeagent.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * AI生成的结果类，用于封装AI返回的内容
 */
@Data
@Description("生成多个代码文件的结果")
public class MultiFileCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JS代码")
    private String jsCode;

    @Description("生成的代码的描述")
    private String description;
}
