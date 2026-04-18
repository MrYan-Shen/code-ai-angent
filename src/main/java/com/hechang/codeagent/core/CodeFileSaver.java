package com.hechang.codeagent.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 代码文件保存类
 */
public class CodeFileSaver {

    //文件保存的根目录
    private static final String CODE_FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/src/main/tmp/code_output";

    /**
     * 保存 Html代码结果
     * @param result 待保存的Html代码结果
     * @return 保存后的目录
     */
    public static File saveHtmlCodeResult(HtmlCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());

        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }

    /**
     * 保存多文件代码结果
     * @param result 待保存的多文件代码结果
     * @return 保存后的目录
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());

        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());

        return new File(baseDirPath);
    }

    /**
     * 构建一个唯一的目录（用时间+雪花id确保唯一性）: tmp/code_output/bizType_时间_雪花ID
     * @return 构建的目录
     */
    private static String buildUniqueDir(String bizType) {
        //构建目录名称
        String uniqueDirName = StrUtil.format("{}_{}_{}",
                bizType, LocalDateTime.now().toString(), IdUtil.getSnowflakeNextIdStr());
        //构建目录路径
        String dirPath = CODE_FILE_SAVE_ROOT_PATH + File.separator + uniqueDirName;
        //创建目录
        FileUtil.mkdir(dirPath);
        //返回目录路径
        return dirPath;
    }

    /**
     * 实现写入文件
     * @param dirPath 目录路径
     * @param filename 文件名
     * @param content 文件内容
     */
    public static void writeToFile(String dirPath, String filename, String content){
        String filePath = dirPath + File.separator + filename;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }
}
