package com.hechang.codeagent.core.saver;

import cn.hutool.core.util.StrUtil;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import com.hechang.codeagent.core.CodeFileSaver;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        // 1. 保存HTML代码
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        // 2. 保存CSS代码
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        // 3. 保存JS代码
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有HTML代码，CSS 、JS 代码可以为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"HTML代码结果不能为空");
        }
    }

}
