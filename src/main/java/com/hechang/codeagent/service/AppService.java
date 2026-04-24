package com.hechang.codeagent.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.hechang.codeagent.model.dto.app.AppAddRequest;
import com.hechang.codeagent.model.dto.app.AppQueryRequest;
import com.hechang.codeagent.model.entity.App;
import com.hechang.codeagent.model.entity.User;
import com.hechang.codeagent.model.vo.AppVO;
import reactor.core.publisher.Flux;


import java.io.Serializable;
import java.util.List;

/**
 * 应用 服务层。
 *
 * @author chang
 */
public interface AppService extends IService<App> {

    /**
     * 获取应用封装类
     *
     * @param app  app
     * @return 封装类
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList app列表
     * @return 列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest app查询条件
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 调用AI生成代码模块的门面代码来生成代码
     *
     * @param appId  appId
     * @param userMessage 用户输入
     * @param loginUser 登录用户
     * @return 列表
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);

    /**
     * 部署代码
     *
     * @param appId  appId
     * @param loginUser 登录用户
     * @return 列表
     */
    String deployApp(Long appId, User loginUser);

    /**
     * 重写mybatis-flex的删除方法，添加容错设计，
     * 即使对话历史删除失败，也不会阻止应用的删除操作，只是记录错误日志，确保核心业务的稳定性
     *
     * @param id  id
     * @return 列表
     */
    boolean removeById(Serializable id);

    /**
     * 异步生成应用截图
     *
     * @param appId appId
     * @param appUrl appUrl
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 添加应用
     *
     * @param appAddRequest app添加请求
     * @param loginUser 登录用户
     * @return 列表
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);
}