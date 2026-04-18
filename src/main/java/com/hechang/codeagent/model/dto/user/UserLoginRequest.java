package com.hechang.codeagent.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后端接受前端的用户登录请求参数的类
 */
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3191241716373120793L;

    //账号
    private String userAccount;

    //密码
    private String userPassword;
}
