package com.hechang.codeagent.core.handler;

import com.hechang.codeagent.model.entity.User;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;
import com.hechang.codeagent.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 *  根据代码生成类型创建合适的流处理器
 *  1.传统的 Flux<String>流（HTML、MULTI_FILE） -> SimpleTextStreamHandler
 *  2.TokenStream 格式的复杂流（VUE_PROJECT） -> JsonMessageStreamHandler
 */
@Slf4j
@Component
public class StreamHandlerExecutor {
    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    public Flux<String> execute(Flux<String> originFlux,
                                ChatHistoryService chatHistoryService,
                                long appId, User loginUser,
                                CodeGenTypeEnum codeGenType) {
        // 根据代码生成类型创建合适的流处理器
        return switch (codeGenType){
            case HTML, MULTI_FILE -> new SimpleTextStreamHandler()
                    .handle(originFlux, chatHistoryService, appId, loginUser);
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originFlux, chatHistoryService, appId, loginUser);
        };
    }
}
