package com.hechang.codeagent.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hechang.codeagent.annotation.AuthCheck;
import com.hechang.codeagent.common.BaseResponse;
import com.hechang.codeagent.common.DeleteRequest;
import com.hechang.codeagent.common.ResultUtils;
import com.hechang.codeagent.constant.UserConstant;
import com.hechang.codeagent.exception.BusinessException;
import com.hechang.codeagent.exception.ErrorCode;
import com.hechang.codeagent.exception.ThrowUtils;
import com.hechang.codeagent.model.dto.user.*;
import com.hechang.codeagent.model.vo.LoginUserVO;
import com.hechang.codeagent.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.hechang.codeagent.model.entity.User;
import com.hechang.codeagent.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户 控制层。
 *
 * @author Chang
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        return ResultUtils.success(
                userService.userRegister(userAccount, userPassword, checkPassword)
        );
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        return ResultUtils.success(
                userService.userLogin(userAccount, userPassword,request)
        );
    }

    /**
     * 获取当前登录用户 (返回的是脱敏后的用户信息)
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getCurrentUser(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(userService.userLogout(request));
    }



    /**
     * 创建用户,仅管理员
     *
     * @param userAddRequest 用户添加请求
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        //设置默认密码
        final String defaultPassword = "12345678";
        //密码加密
        user.setUserPassword(userService.getEncryptPassword(defaultPassword));

        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success( user.getId());
    }

    /**
     * 根据 id 获取用户。仅管理员
     *
     * @param id 用户主键
     * @return 用户详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {

        BaseResponse<User> response = this.getUserById(id);

        User user = response.getData();

        UserVO userVO = userService.getUserVO(user);
        return ResultUtils.success(userVO);
    }

    /**
     * 根据 id 删除用户。仅管理员
     *
     * @param deleteRequest 删除请求
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userService.removeById(deleteRequest.getId()));
    }

    /**
     * 根据 id 更新用户。仅管理员
     *
     * @param userUpdateRequest 更新请求
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        //抛出异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页查询用户。仅管理员
     *
     * @param userQueryRequest 查询请求
     * @return 分页对象
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        return ResultUtils.success(userService.listUserVO(userQueryRequest));
    }

}
