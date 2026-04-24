package com.hechang.codeagent.utils;

import com.hechang.codeagent.constant.AppConstant;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.entity.App;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * 目录工具类
 */
@Slf4j
public class DirectoryUtils {

    /**
     * 获取代码生成路径
     * @return 代码生成路径
     */
    public static File getCodeGeneratePath(App app, Long appId) {
        // 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();

        // 在 code_output 目录下查找匹配的文件夹
        File codeOutputDir = new File(AppConstant.CODE_OUTPUT_ROOT_DIR);
        if (!codeOutputDir.exists() || !codeOutputDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码输出目录不存在");
        }

        File sourceDir = null;

        // VUE_PROJECT 类型使用固定目录名，其他类型使用前缀匹配
        if ("vue_project".equals(codeGenType)) {
            // Vue 项目使用固定目录名：vue_project_{appId}
            String dirName = codeGenType + "_" + appId;
            sourceDir = new File(codeOutputDir, dirName);
            log.info("Vue项目，尝试查找目录: {}", sourceDir.getAbsolutePath());
        } else {
            // 其他类型使用前缀匹配：codeGenType_appId_
            String namePrefix = codeGenType + "_" + appId + "_";

            // 查找所有以前缀开头的文件夹，并选择最后修改时间最新的
            long latestModified = 0;
            File[] matchedDirs = codeOutputDir.listFiles((dir, name) ->
                    dir.isDirectory() && name.startsWith(namePrefix)
            );

            if (matchedDirs != null) {
                // 选择最后修改时间最新的文件夹
                for (File dir : matchedDirs) {
                    if (dir.lastModified() > latestModified) {
                        latestModified = dir.lastModified();
                        sourceDir = dir;
                    }
                }
            }
        }

        // 检查是否找到源目录
        if (sourceDir == null || !sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }

        log.info("找到应用代码目录: {}", sourceDir.getAbsolutePath());

        return sourceDir;
    }
}
