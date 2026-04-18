package com.hechang.codeagent.service;

import com.hechang.codeagent.model.vo.LoginUserVO;
import com.mybatisflex.core.service.IService;
import com.hechang.codeagent.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户 服务层。
 *
 * @author Chang
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount
     * 用户账户
     * @param userPassword
     * 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 获取加密密码
     *
     * @param userPassword: 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取脱敏后的当前登录用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     * @param userAccount: 用户账户
     * @param userPassword: 用户密码
     * @param request: 请求
     * @return 用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request: 请求
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request: 请求
     */
    boolean userLogout(HttpServletRequest request);
}
