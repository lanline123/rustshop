package com.tigshop.bean.enums.user;

import com.tigshop.common.exception.GlobalException;
import lombok.Getter;


/**
 * @author Admin
 */

@Getter
public enum AuthorizeTypeEnum {
    /**
     * 公众号
     */
    WECHAT(1, "wechat"),
    /**
     * PC 微信公众扫码注册时使用
     */
    PC(1, "pc"),
    /**
     * 小程序
     */
    MINI_PROGRAM(2, "miniProgram"),
    /**
     * PC
     */
    GOOGLE(101, "GOOGLE"),
    /**
     * Android
     */
    FACEBOOK(102, "FACEBOOK"),
    /**
     * X / Twitter
     */
    TWITTER(103, "TWITTER"),
    ;

    private final int code;
    private final String description;

    AuthorizeTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }


    public static Integer getAuthorizeType(String description) {
        for (AuthorizeTypeEnum tag : values()) {
            if (tag.getDescription().equals(description)) {
                return tag.getCode();
            }
        }
        return -1;
    }

    public static AuthorizeTypeEnum fromCode(String description) {
        for (AuthorizeTypeEnum type : values()) {
            if (type.description.equals(description)) {
                return type;
            }
        }
        throw new GlobalException("该平台暂不支持");
    }
}
