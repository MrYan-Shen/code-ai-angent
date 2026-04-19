package com.hechang.codeagent.core;

import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器（因为流式输出返回的事字符串片段，需要我们在AI全部返回完成后进行解析）
 * 提供静态方法解析不同类型的代码内容
 * 核心逻辑：通过正则表达式从完整字符串中提取到对应的代码块，并返回结构化输出对象，从而复用之前的文件保存器
 */
public class CodeParser {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析Html单文件代码
     * @param codeContent 待解析的代码内容
     * @return 解析后的Html代码结果
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取Html代码
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()){
            result.setHtmlCode(htmlCode.trim());
        }else {
            // 如果没有提取到Html代码，则将完整内容作为Html代码
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 解析多文件代码
     * @param codeContent 待解析的代码内容
     * @return 解析后的多文件代码结果
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 提取各类代码
        String htmlCode = extractCodeByRegex(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractCodeByRegex(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractCodeByRegex(codeContent, JS_CODE_PATTERN);
        //设置各类代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()){
            result.setHtmlCode(htmlCode.trim());
        }
        if (cssCode != null && !cssCode.trim().isEmpty()){
            result.setCssCode(cssCode.trim());
        }
        if (jsCode != null && !jsCode.trim().isEmpty()){
            result.setJsCode(jsCode.trim());
        }

        return result;
    }

    /**
     * 从完整字符串中提取代码块
     * @param codeContent 原始内容
     * @return 提取的代码块
     */
    private static String extractHtmlCode(String codeContent) {
        Matcher matcher = HTML_CODE_PATTERN.matcher(codeContent);
        if (matcher.find()){
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 根据正则表达式提取代码
     */
    private static String extractCodeByRegex(String codeContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(codeContent);
        if (matcher.find()){
            return matcher.group(1);
        }
        return null;
    }
}
