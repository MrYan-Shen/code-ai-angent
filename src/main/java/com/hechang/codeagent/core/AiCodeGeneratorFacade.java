package com.hechang.codeagent.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hechang.codeagent.ai.AiCodeGeneratorService;
import com.hechang.codeagent.ai.AiCodeGeneratorServiceFactory;
import com.hechang.codeagent.ai.model.HtmlCodeResult;
import com.hechang.codeagent.ai.model.MultiFileCodeResult;
import com.hechang.codeagent.ai.model.message.AiResponseMessage;
import com.hechang.codeagent.ai.model.message.ToolExecutedMessage;
import com.hechang.codeagent.ai.model.message.ToolRequestMessage;
import com.hechang.codeagent.constant.AppConstant;
import com.hechang.codeagent.core.builder.VueProjectBuilder;
import com.hechang.codeagent.core.parser.CodeParserExecutor;
import com.hechang.codeagent.core.saver.CodeFileSaverExecutor;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
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
//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;
    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    //针对单文件和多文件的生成模式（非流式），各提供一个“生成代码并保存”的方法，核心逻辑是：拼接AI实时响应的字符串，并在流式返回完成后解析字符串并保存代码文件
    /**
     * 生成并保存Html代码
     * @param userMessage 用户输入
     * @return 保存的目录
     *//*
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateCode(userMessage);

        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }

    *//**
     * 生成并保存多文件代码
     * @param userMessage 用户输入
     * @return 保存的目录
     *//*
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);

        return CodeFileSaver.saveMultiFileCodeResult(multiFileCodeResult);
    }

    *//**
     * 统一入口：生成代码并保存，非流式
     * @param userMessage 用户输入
     * @param codeGenType 生成模式
     * @return 保存的目录
     *//*
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
    }*/


    //针对单文件和多文件的生成模式（流式），各提供一个“生成代码并保存”的方法，核心逻辑是：拼接AI实时响应的字符串，并在流式返回完成后解析字符串并保存代码文件
    /**
     * 生成 HTML 模式的代码并保存（流式）
     * @param userMessage 用户输入
     * @return 保存的目录
     *//*
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

    *//**
     * 生成 多文件 模式的代码并保存（流式）
     * @param userMessage 用户输入
     * @return 保存的目录
     *//*
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

    *//**
     * 统一入口：生成代码并保存（流式）
     * @param userMessage 用户输入
     * @param codeGenType 生成模式
     * @return 保存的目录
     *//*
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
    }*/

    //优化后
    /**
     * 通过流式代码处理代码保存文件 (优化后)
     * @param codeStream 代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenType, Long appId){
        // HTML 和多文件模式需要解析和保存
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream
                .doOnNext(codeBuilder::append) //实时收集代码片段
                .doOnComplete(() -> {
                    //流式返回完成后，解析字符串并保存代码文件
                    try {
                        String completeCode = codeBuilder.toString();
                        //使用执行器解析代码
                        Object parserResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                        //使用执行器保存代码
                        File saveDir = CodeFileSaverExecutor.executeSaver(parserResult,codeGenType, appId);
                        log.info("代码保存成功，保存目录为：{}", saveDir.getAbsolutePath());
                    } catch (Exception e) {
                        log.error("代码保存失败：{}", e.getMessage(), e);
                    }
                });
    }

    /**
     * 统一入口：根据类型生成并保存代码（非流式-优化后）
     * @param userMessage 用户输入
     * @param codeGenType 生成类型
     * @return 保存的目录
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"请选择生成模式");
        }
        // 根据 类型 返回生成的代码
        return switch (codeGenType) {
            case HTML -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
                HtmlCodeResult result = aiCodeGeneratorService.generateCode(userMessage);
                //yield:可用于返回值
                yield CodeFileSaverExecutor.executeSaver(result, codeGenType, appId);
            }
            case MULTI_FILE -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, codeGenType, appId);
            }
            case VUE_PROJECT -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenType);
                // Vue 项目使用工具调用写入文件，需要等待流式响应完成
                TokenStream result = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                // 阻塞等待所有文件写入完成
                // 直接返回项目目录
                yield CodeFileSaverExecutor.executeSaver(result, codeGenType, appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码（流式-优化后）
     * @param userMessage 用户输入
     * @param codeGenType 生成类型
     * @return 保存的目录
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenType, Long appId) {
        if (codeGenType == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR ,"请选择生成模式");
        }
        // 根据 类型 返回生成的代码
        return switch (codeGenType) {
            case HTML -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
                Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                //yield:可用于返回值
                yield processCodeStream(result, codeGenType, appId);
            }
            case MULTI_FILE -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId);
                Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(result, codeGenType, appId);
            }
            case VUE_PROJECT -> {
                AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenType);
                // Vue 项目使用工具调用写入文件，需要等待流式响应完成
                TokenStream result = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                // 文件保存由 AI 工具自动完成，不需要额外处理
                yield processTokenStream(result,appId);
            }
            default -> {
                String errorMessage = "不支持的生成类型" + codeGenType.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR ,errorMessage);
            }
        };
    }

    /**
     * 将 TokenStream 转换为 Flux<String> (适配器模式)
     * @param tokenStream TokenStream
     * @return Flux<String>
     */
    private Flux<String> processTokenStream(TokenStream tokenStream,Long appId){
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        // 执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        log.error("processTokenStream error: {}", error.getMessage());
                        sink.error(error);
                    })
                    .start();
        });
    }



}
