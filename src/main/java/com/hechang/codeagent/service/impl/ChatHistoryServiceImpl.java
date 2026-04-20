package com.hechang.codeagent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.hechang.codeagent.model.entity.ChatHistory;
import com.hechang.codeagent.mapper.ChatHistoryMapper;
import com.hechang.codeagent.service.ChatHistoryService;
import org.springframework.stereotype.Service;

/**
 * 对话历史 服务层实现。
 *
 * @author Chang
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

}
