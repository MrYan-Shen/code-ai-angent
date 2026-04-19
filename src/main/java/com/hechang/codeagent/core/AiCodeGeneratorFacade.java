package com.hechang.codeagent.core;

import com.hechang.codeagent.ai.AiCodeGeneratorService;
import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI代码生成器外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    //针对单文件和多文件的生成模式（非流式），各提供一个“生成代码并保存”的方法，核心逻辑是：拼接AI实时响应的字符串，并在流式返回完成后解析字符串并保存代码文件
    /**
     * 生成并保存Html代码
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateCode(userMessage);

        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    /**
     * 生成并保存多文件代码
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);

        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

    /**
     * 统一入口：生成代码并保存，非流式
     * @param userMessage 用户输入
     * @param codeGenType 生成模式
     * @return 保存的目录
     */
    public File generateCode(String userMessage, CodeGenTypeEnum codeGenType) {
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"请选择生成模式");
        }
        // 根据 类型 返回生成的代码
        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,errorMessage);
            }
        };
    }


    //针对单文件和多文件的生成模式（流式），各提供一个“生成代码并保存”的方法，核心逻辑是：拼接AI实时响应的字符串，并在流式返回完成后解析字符串并保存代码文件
    /**
     * 生成 HTML 模式的代码并保存（流式）
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        //拼接AI实时响应的流
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(codeBuilder::append) //实时收集代码片段
                .doOnComplete(() -> {
                    //流式返回完成后，解析字符串并保存代码文件
                    try {
                        String completeHtmlCode = codeBuilder.toString();
                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                        //保存代码到文件中
                        File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                        log.info("代码保存成功，保存目录为：{}", saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("代码保存失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 生成 多文件 模式的代码并保存（流式）
     * @param userMessage 用户输入
     * @return 保存的目录
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //拼接AI实时响应的流
        StringBuilder codeBuilder = new StringBuilder();
        return result
                .doOnNext(codeBuilder::append) //实时收集代码片段
                .doOnComplete(() -> {
                    //流式返回完成后，解析字符串并保存代码文件
                    try {
                        String completeMultiFileCode = codeBuilder.toString();
                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                        //保存代码到文件中
                        File saveDir = CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
                        log.info("代码保存成功，保存目录为：{}", saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("代码保存失败：{}", e.getMessage());
                    }
                });
    }

    /**
     * 统一入口：生成代码并保存（流式）
     * @param userMessage 用户输入
     * @param codeGenType 生成模式
     * @return 保存的目录
     */
    public Flux<String> generateCodeAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType) {
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"请选择生成模式");
        }
        // 根据 类型 获取生成的代码流
        return switch (codeGenType) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,errorMessage);
            }
        };
    }

}
