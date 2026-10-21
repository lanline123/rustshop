package com.tigshop.common.enums;

import lombok.Getter;

/**
 * @author Jayce
 * @create 2024/9/26 13:42
 */
@Getter
public enum LoginTypes {
    PASSWORD("password"), MOBILE("mobile");

    private final String type;

    LoginTypes(String type) {
        this.type = type;
    }

}
