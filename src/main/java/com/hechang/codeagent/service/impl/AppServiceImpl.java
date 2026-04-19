package com.hechang.codeagent.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.hechang.codeagent.model.entity.App;
import com.hechang.codeagent.mapper.AppMapper;
import com.hechang.codeagent.service.AppService;
import org.springframework.stereotype.Service;

/**
 * 应用 服务层实现。
 *
 * @author Chang
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
