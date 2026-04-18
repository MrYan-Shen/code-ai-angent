package com.hechang.codeagent.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    USER("用户","user"),
    ADMIN("管理员","admin");

    private final String text;

    private final String value;

    /**
     * 根据 value 获取枚举项
     *      如果枚举值特别多，可以 Map 缓存所有的枚举值来加速查找，而不是遍历列表
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)){
            return null;
        }
        for (UserRoleEnum anEnum : UserRoleEnum.values()){
            if (anEnum.value.equals(value)){
                return anEnum;
            }
        }
        return null;
    }
}
