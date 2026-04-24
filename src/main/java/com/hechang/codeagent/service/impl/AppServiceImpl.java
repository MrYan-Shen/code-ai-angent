package com.hechang.codeagent.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hechang.codeagent.core.builder.VueProjectBuilder;
import com.hechang.codeagent.core.handler.StreamHandlerExecutor;
import com.hechang.codeagent.model.enums.ChatHistoryMessageTypeEnum;
import com.hechang.codeagent.service.ChatHistoryService;
import com.hechang.codeagent.service.ScreenshotService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.hechang.codeagent.constant.AppConstant;
import com.hechang.codeagent.core.AiCodeGeneratorFacade;

import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.exception.ThrowUtils;
import com.hechang.codeagent.model.dto.app.AppAddRequest;
import com.hechang.codeagent.model.dto.app.AppQueryRequest;
import com.hechang.codeagent.model.entity.App;
import com.hechang.codeagent.mapper.AppMapper;
import com.hechang.codeagent.model.entity.User;
import com.hechang.codeagent.model.enums.CodeGenTypeEnum;
import com.hechang.codeagent.model.vo.AppVO;
import com.hechang.codeagent.model.vo.UserVO;
import com.hechang.codeagent.service.AppService;
import com.hechang.codeagent.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author chang
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private ScreenshotService screenshotService;

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 优化——使用 StreamHandlerExecutor 解析代码
     * @param appId  appId
     * @param userMessage 用户输入
     * @param loginUser 登录用户
     * @return Flux<String>
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser) {
        // 校验
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 验证用户是否有权限访问该应用，仅应用创建者可以生成代码
        if (!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限访问该应用");
        }
        // 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        }
        // 通过效验后，添加用户消息到对话历史
        chatHistoryService.addChatMessage(appId, userMessage, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 调用 AI 生成代码(流式)
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        // 收集AI响应得到内容并记录到对话历史
        return streamHandlerExecutor.execute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 参数验证
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 获取应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 验证用户是否有权限访问该应用，仅应用创建者可以生成代码
        if (!app.getUserId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限部署该应用");
        }
        // 检查是否已有deployKey，如果没有则生成新的
        String deployKey = app.getDeployKey();
        boolean isRedeploy = StrUtil.isNotBlank(deployKey);
        if (!isRedeploy) {
            // 首次部署，生成6位的deployKey (大小写字母 + 数字)
            deployKey = RandomUtil.randomString(6);
        } else {
            log.info("应用已存在deployKey: {}，将进行重新部署", deployKey);
        }
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
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT){
            // 处理Vue项目
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDir.getAbsolutePath());
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue项目构建失败");
            // 检查dist目录是否存在
            File distDir = new File(sourceDir, "dist");
            ThrowUtils.throwIf(!distDir.exists() || !distDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "Vue项目构建完成单但未生成dist目录");
            // 将dist目录作为部署源
            sourceDir = distDir;
            log.info("找到Vue项目代码目录: {}", sourceDir.getAbsolutePath());
        }

        // 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"部署失败：" + e.getMessage());
        }
        // 更新应用的deployKey和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR,"更新应用部署信息失败");
        // 返回可访问的 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 异步生成截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }

    /**
     * 异步生成应用截图 —— 使用java21的新特性虚拟线程
     *
     * @param appId appId
     * @param appUrl appUrl
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 创建虚拟线程异步执行
        Thread.ofVirtual().start(() -> {
           // 调用截图服务生成截图
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updateResult = this.updateById(updateApp);
            ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR,"更新应用封面失败");
        });
    }


    @Override
    public boolean removeById(Serializable id) {
        if (id == null) {
            return false;
        }
        // 转换为 Long 类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }
        //先删除关联的对话历史
        try {
            chatHistoryService.deleteByAppId(appId);
        } catch (Exception e) {
            //记录日志但不阻止应用删除
             log.error("删除应用关联对话历史失败：{}",e.getMessage());
        }
        //删除应用
        return super.removeById(id);
    }


}
