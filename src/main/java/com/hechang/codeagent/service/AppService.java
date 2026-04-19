package com.hechang.codeagent.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.hechang.codeagent.model.dto.app.AppAddRequest;
import com.hechang.codeagent.model.dto.app.AppQueryRequest;
import com.hechang.codeagent.model.entity.App;
import com.hechang.codeagent.model.entity.User;
import com.hechang.codeagent.model.vo.AppVO;
import reactor.core.publisher.Flux;


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
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

}