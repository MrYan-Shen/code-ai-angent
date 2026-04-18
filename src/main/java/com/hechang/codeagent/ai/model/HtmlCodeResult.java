package com.hechang.codeagent.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * AI生成的结果类，用于封装AI返回的内容
 */
@Data
@Description("生成 html 代码文件的结果")
public class HtmlCodeResult {

    @Description("生成的 html 代码")
    private String htmlCode;

    @Description("生成的 html 代码的描述")
    private String description;
}
