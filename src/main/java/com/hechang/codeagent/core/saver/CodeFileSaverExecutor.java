package com.hechang.codeagent.core.saver;

import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存器执行器
 * 根据代码生成类型，选择对应的保存器
 */
public class CodeFileSaverExecutor {
    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();
    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();
    private static final VueProjectCodeFileSaverTemplate vueProjectCodeFileSaver = new VueProjectCodeFileSaverTemplate();

    /**
     * 执行代码保存
     * @param codeResult 代码结果对象（可以是 HtmlCodeResult、MultiFileCodeResult）
     * @param codeGenType 代码生成类型枚举
     * @return 保存后的目录
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType, Long appId){
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult, appId);
            case VUE_PROJECT -> // Vue 项目文件已通过 AI 工具调用写入，直接返回项目目录路径
                    vueProjectCodeFileSaver.getProjectDir(appId);
            default -> throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型：" + codeGenType
            );
        };
    }
}
