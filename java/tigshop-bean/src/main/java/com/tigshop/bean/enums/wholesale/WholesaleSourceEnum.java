package com.tigshop.bean.enums.wholesale;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 批发商来源枚举：1=注册自动 2=后台导入 3=手动添加
 *
 * @author Tigshop团队
 * @create 2026-06-02
 */
@Getter
@AllArgsConstructor
public enum WholesaleSourceEnum {

    AUTO_REGISTER(1, "注册自动"),
    IMPORT(2, "后台导入"),
    MANUAL(3, "手动添加");

    @JsonValue
    private final Integer code;
    private final String desc;

    public static WholesaleSourceEnum fromCode(Integer code) {
        for (WholesaleSourceEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
